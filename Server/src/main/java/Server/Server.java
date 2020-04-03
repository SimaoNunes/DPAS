package Server;

import Library.Envelope;
import Library.Request;
import Library.Response;

import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileReader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable{

    private ServerSocket server = null;
    private Map<PublicKey, String> userIdMap = null;
    private AtomicInteger TotalAnnouncements;
    private CriptoManager criptoManager = null;

    protected Server(ServerSocket ss){
        server = ss;
        criptoManager = new CriptoManager();
        getUserIdMap();
        getTotalAnnouncementsFromFile();
        newListener();
    }
    
//////////////////////////////////////////
//  								    //
//         Main method running          //
//    									//
//////////////////////////////////////////

    public void run(){

        Socket socket = null;
        ObjectOutputStream outStream;
        ObjectInputStream inStream;

        try{
            socket = server.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }

        newListener();

        try {
            inStream = new ObjectInputStream(socket.getInputStream());
            outStream = new ObjectOutputStream(socket.getOutputStream());
            try {
                System.out.println("User connected.");
                Envelope envelope = (Envelope) inStream.readObject();

                switch(envelope.getRequest().getOperation()) {
                    case "REGISTER":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            criptoManager.checkHash(envelope, outStream) && 
                            criptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-2}))
                            {
                            register(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POST":
                        System.out.println("POST OPERATION");
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            criptoManager.checkHash(envelope, outStream) && 
                            criptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-1, -4, -5})) 
                            {
                            post(envelope.getRequest(), false, outStream);
                        }
                        break;
                    case "POSTGENERAL":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            criptoManager.checkHash(envelope, outStream) && 
                            criptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-1, -4, -5}))
                            {
                            post(envelope.getRequest(), true, outStream);
                        }
                        break;
                    case "READ":
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            criptoManager.checkHash(envelope, outStream) && 
                            criptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-3, -6, -10}))
                            {
                            read(envelope.getRequest(), false, outStream);
                        }
                        break;
                    case "READGENERAL":
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-6, -10})) {
                            read(envelope.getRequest(), true, outStream);
                        }
                        break;
                    case "NONCE":
                        byte[] randomNonce = criptoManager.generateRandomNonce(envelope.getRequest().getPublicKey());
                        send(new Response(randomNonce), outStream);
                        break;
                    case "DELETEALL":
                        deleteUsers();
                        break;
                    case "SHUTDOWN":
                        shutDown();
                        break;
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

//////////////////////////////////////////
//  									//
//             API Methods              //
//	                                    //
//////////////////////////////////////////

    //////////////////////////////////////////////////
    //				    REGISTER					//
    //////////////////////////////////////////////////
    
    public void register(Request request, ObjectOutputStream outStream) {
        System.out.println("REGISTER Method. Registering user: " + request.getName());

        synchronized (userIdMap) {
            String username = criptoManager.checkKey(request.getPublicKey());
            String path = "./storage/AnnouncementBoards/" + username;
            File file = new File(path);
            file.mkdirs();
            userIdMap.put(request.getPublicKey(), username);
            saveUserIdMap();
            System.out.println("User " + request.getName() + " successfully registered!");
            send(new Response(true, request.getNonceClient()), outStream);
        }
    }

    //////////////////////////////////////////////////
    //				      POST						//
    //////////////////////////////////////////////////
    
    private void post(Request request, Boolean general, ObjectOutputStream outStream){
        // Get userName from keystore
        String username = userIdMap.get(request.getPublicKey());
        String path = "./storage/AnnouncementBoards/" + username + "/";
        
        // Write to file
        JSONObject announcementObject =  new JSONObject();
        announcementObject.put("id", Integer.toString(getTotalAnnouncements()));
        announcementObject.put("user", username);
        announcementObject.put("message", request.getMessage());
        
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy 'at' HH:mm");
        announcementObject.put("date", ft.format(dNow).toString());

        int[] ref_announcements = request.getAnnouncements();
        if(ref_announcements != null){
            JSONArray annoucementsList = new JSONArray();
            for(int i = 0; i < ref_announcements.length; i++){
                annoucementsList.add(Integer.toString(ref_announcements[i]));
            }
            announcementObject.put("ref_announcements", annoucementsList);
        }

        if(general){
            path = "./storage/GeneralBoard/";
        }

        try {
            saveFile(path + Integer.toString(getTotalAnnouncements()), announcementObject.toJSONString()); //GeneralBoard
        } catch (IOException e) {
            send(new Response(false, -9, request.getNonceClient()), outStream);
        }

        incrementTotalAnnouncs();
        saveTotalAnnouncements();

        send(new Response(true, request.getNonceClient()), outStream);
    }
    
    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////
    
    private void read(Request request, boolean isGeneral, ObjectOutputStream outStream) {

        String[] directoryList = getDirectoryList(request.getPublicKey());
        int directorySize = directoryList.length;

        String path = "./storage/";
        if(!isGeneral){
            System.out.println("READ method");
            String username = userIdMap.get(request.getPublicKey());
            path += "AnnouncementBoards/" + username + "/";
        } else{
            System.out.println("READGENERAL method");
            path += "GeneralBoard/";

        }

        int total;
        if(request.getNumber() == 0) { //all posts
            total = directorySize;
        } else {
            total = request.getNumber();
        }

        Arrays.sort(directoryList);
        JSONParser parser = new JSONParser();
        try{
            JSONArray annoucementsList = new JSONArray();
            JSONObject announcement;

            String fileToRead;
            for (int i=0; i<total; i++) {
                fileToRead = directoryList[directorySize-1]; // -1 because arrays starts in 0
                announcement = (JSONObject) parser.parse(new FileReader(path + fileToRead));
                directorySize--;
                annoucementsList.add(announcement);
            }
            JSONObject announcementsToSend =  new JSONObject();
            announcementsToSend.put("announcementList", annoucementsList);
            send(new Response(true, announcementsToSend, request.getNonceClient()), outStream);
        } catch(Exception e){
            e.printStackTrace();
            send(new Response(false, -8, request.getNonceClient()), outStream);
        }
    }
    
//////////////////////////////////////////
//										//
//           Auxiliary Methods          //
//    									//
//////////////////////////////////////////

    private Boolean checkValidAnnouncements(int[] announcs){
        int total = getTotalAnnouncements();
        for (int i = 0; i < announcs.length; i++) { 		      
            if (announcs[i] >= total ) {
                return false;
            }		
        } 	
        return true;
    }
    
    private String[] getDirectoryList(PublicKey key){
        String path = "./storage/";
        if(key == null) {
            path += "GeneralBoard/";
        }
        else {
            path += "AnnouncementBoards/" + userIdMap.get(key) + "/";
        }

        File file = new File(path);
        return file.list();
    }

    private void send(Response response, ObjectOutputStream outputStream){
        MessageDigest md ;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;

        Cipher cipher;

        try {
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(response);
            out.flush();

            byte[] request_bytes = bos.toByteArray();
            byte[] response_hash = md.digest(request_bytes);

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, criptoManager.getPrivateKey());

            byte[] final_bytes = cipher.doFinal(response_hash);

            outputStream.writeObject(new Envelope(response, final_bytes));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

    }

    private void saveFile(String completePath, String announcement) throws IOException {
        byte[] bytesToStore = announcement.getBytes();
        File file = new File(completePath);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytesToStore);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
    
    private void newListener() {
        (new Thread(this)).start();
    }
    
////////////////////////////////////////////////////////////////////////////////
//   									                                      //
//  Method used to delete Tests' populate && Shut down Server && Start server //
//										                                      //
////////////////////////////////////////////////////////////////////////////////

    public void deleteUsers() throws IOException {

        System.out.println("DELETE operation");

        userIdMap.clear();
        saveUserIdMap();

        String path = "./storage/AnnouncementBoards";

        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();

        path = "./storage/GeneralBoard";

        FileUtils.deleteDirectory(new File(path));
        files = new File(path);
        files.mkdirs();

        path = "./storage/";
        setTotalAnnouncements(0);
        saveTotalAnnouncements();
    }

    private void shutDown(){
        System.out.println("Shut down operation");
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name.split("@")[0]);

        try {
            Runtime.getRuntime().exec("kill -SIGINT " + name.split("@")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
/////////////////////////////////////////////
//										   //
// Methods to save/get userIdMap from File //
//										   //
/////////////////////////////////////////////

    
    private void saveUserIdMap() {
        try {
            FileOutputStream fileCopy = new FileOutputStream("./storage/UserIdMap_copy.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileCopy);
            out.writeObject(userIdMap);
            out.close();
            fileCopy.close();
            System.out.println("Created updated copy of the userIdMap");

            File original = new File("./storage/UserIdMap.ser");
            File copy = new File("./storage/UserIdMap_copy.ser");

            if(original.delete()){
                copy.renameTo(original);
            }

         } catch (IOException i) {
            i.printStackTrace();
         }
    }
    
    private void getUserIdMap() {
        try {
           FileInputStream fileIn = new FileInputStream("./storage/UserIdMap.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           userIdMap = (Map<PublicKey, String>) in.readObject();
           in.close();
           fileIn.close();
        } catch (ClassNotFoundException c) {
           System.out.println("Map<PublicKey, String> class not found");
           c.printStackTrace();
           return;
        }
        catch(FileNotFoundException e){
            userIdMap = new HashMap<PublicKey, String>();
            createOriginalUserMap();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOriginalUserMap() {

        FileOutputStream fileCopy = null;
        try {
            fileCopy = new FileOutputStream("./storage/UserIdMap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileCopy);
            out.writeObject(userIdMap);
            out.close();
            fileCopy.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

/////////////////////////////////////////////////////////
//										               //
// Methods to get/update total announcements from File //
//										               //
/////////////////////////////////////////////////////////


    private void incrementTotalAnnouncs(){
        TotalAnnouncements.incrementAndGet();
    }

    private void setTotalAnnouncements(int value){
        TotalAnnouncements.set(value);
    }

    private int getTotalAnnouncements(){
        return TotalAnnouncements.get();
    }

    private void saveTotalAnnouncements(){
        try {
            FileOutputStream fileOut = new FileOutputStream("./storage/TotalAnnouncements_copy.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(TotalAnnouncements.get());
            out.close();
            fileOut.close();
            System.out.println("Serialized data saved in copy");

            File original = new File("./storage/TotalAnnouncements.ser");
            File copy = new File("./storage/TotalAnnouncements_copy.ser");

            if(original.delete()){
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void getTotalAnnouncementsFromFile() {
        try {
           FileInputStream fileIn = new FileInputStream("./storage/TotalAnnouncements.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           int a = (int) in.readObject();
           System.out.println(a);
           TotalAnnouncements = new AtomicInteger(a);
           in.close();
           fileIn.close();
        }
        catch(FileNotFoundException e){
            TotalAnnouncements = new AtomicInteger(0);
            createOriginalAnnouncs();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Total announcements-> " + TotalAnnouncements);
    }

    private void createOriginalAnnouncs(){

        try {
            FileOutputStream fileOut = new FileOutputStream("./storage/TotalAnnouncements.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(TotalAnnouncements.get());
            out.close();
            fileOut.close();

        } catch (IOException i) {
            i.printStackTrace();
        }

    }

    //////////////////////////////////////////
    //  									//
    //          Check Exceptions            //
    //	                                    //
    //////////////////////////////////////////

    public boolean checkExceptions(Request request, ObjectOutputStream outStream, int[] codes){
        for (int i = 0; i < codes.length; i++) {
            switch(codes[i]) {
                // ## UserNotRegistered ## -> check if user is registed
                case -1:
                    if(!userIdMap.containsKey(request.getPublicKey())) {
                        send(new Response(false, -1, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
                // ## AlreadyRegistered ## -> check if user is already registered
                case -2:
                        if (userIdMap.containsKey(request.getPublicKey())) {
                            send(new Response(false, -2, request.getNonceClient()), outStream);
                            return false;
                        }
                    break;
                // ## UserNotRegistered ## -> check if user to read from is registed
                case -3:
                    if(!userIdMap.containsKey(request.getPublicKeyToReadFrom())) {
                        send(new Response(false, -3, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
                // ## MessageTooBig ## -> Check if message length exceeds 255 characters
                case -4:
                    if(request.getMessage().length() > 255) {
                        send(new Response(false, -4, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
                // ## InvalidAnnouncement ## -> checks if announcements refered by the user are valid
                case -5:
                    if(request.getAnnouncements() != null && !checkValidAnnouncements(request.getAnnouncements())) {
                        send(new Response(false, -5, request.getNonceClient()), outStream); 
                        return false;      
                    }
                    break;
                // ## InvalidPostsNumber ## -> check if number of requested posts are bigger than zero
                case -6:
                    if (request.getNumber() < 0) {
                        send(new Response(false, -6, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
                // ## UnknownPublicKey ## -> Check if key is null or known by the Server. If method is read, check if key to ready from is null
                case -7:
                    if (request.getPublicKey() == null || criptoManager.checkKey(request.getPublicKey()) == "" || (request.getOperation().equals("READ") && (request.getPublicKeyToReadFrom() == null || criptoManager.checkKey(request.getPublicKeyToReadFrom()) == ""))) {
                        send(new Response(false, -7, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
                // ## TooMuchAnnouncements ## -> Check if user is trying to read mor announcements that Board number of announcements
                case -10:
                    if (request.getNumber() > getDirectoryList(request.getPublicKey()).length) {
                        send(new Response(false, -10, request.getNonceClient()), outStream);
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
}
