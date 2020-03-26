package Server;

import Library.Request;
import Library.Response;

import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable{

    private ServerSocket server = null;
    private Map<PublicKey, Integer> userIdMap = null;
    private AtomicInteger TotalAnnouncements;

    protected Server(ServerSocket ss){

        getUserIdMap();
        getTotalAnnouncementsFromFile();
        server = ss;
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

                //aqui decifra-se

                Request request = (Request) inStream.readObject();
                switch(request.getOperation()) {
                    case "REGISTER":
                        register(request, outStream);
                        break;
                    case "POST":
                        post(request, false, outStream);
                        break;
                    case "POSTGENERAL":
                        post(request, true, outStream);
                        break;
                    case "READ":
                        read(request, false, outStream);
                        break;
                    case "READGENERAL":
                        read(request, true, outStream);
                        break;
                    case "DELETEALL":
                        deleteUsers();
                        break;
                }
                socket.close();
            }catch (Exception e) {
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
    
    public void register(Request request, ObjectOutputStream outStream) {
        System.out.println("REGISTER Method. Registering user: " + request.getName());
        if(request.getPublicKey() == null || request.getPublicKey().getEncoded().length != 294){
            send(new Response(false, -3), outStream);  //InvalidPublicKey
            return;
        }
        else if (!checkKey(request.getPublicKey())) {
            send(new Response(false, -7), outStream); //key not in server keystore -7
            return;
        }
        synchronized (userIdMap) {
            if (userIdMap.containsKey(request.getPublicKey())) {
                send(new Response(false, -2), outStream);
            }
            // Register new user
            else {
                int userId = userIdMap.size();
                String path = "./storage/AnnouncementBoards/" + userId;
                File file = new File(path);
                file.mkdirs();
                userIdMap.put(request.getPublicKey(), userId);
                saveUserIdMap();
                System.out.println("User " + request.getName() + " successfully registered!");
                send(new Response(true), outStream);
            }

        }
    }
    // se for general, guarda no general e no do user
    // se nao, guarda so no do user
    private void post(Request request, Boolean general, ObjectOutputStream outStream) throws IOException{
        System.out.println("POST method");
        // Check if message length exceeds 255 characters
        if(request.getMessage().length() > 255){
            send(new Response(false, -4), outStream);  //MessageTooBigException
        }
        // Checks if key has proper length
        else if(request.getPublicKey() == null || request.getPublicKey().getEncoded().length != 294){
            send(new Response(false, -3), outStream);  //InvalidPublicKey
        }
        else if(!userIdMap.containsKey(request.getPublicKey())){
            send(new Response(false, -1), outStream);

        }
        else{
            int userId = userIdMap.get(request.getPublicKey());
            String path = "./storage/AnnouncementBoards/" + userId;

            // Write to file
            JSONObject announcementObject =  new JSONObject();
            announcementObject.put("user", userId);
            announcementObject.put("message", request.getMessage());
            saveFile(path + "/" + Integer.toString(getTotalAnnouncements()), announcementObject.toJSONString()); //announcementBoards
            
            if(general){
                path = "./storage/GeneralBoard/";
                saveFile(path + Integer.toString(getTotalAnnouncements()), announcementObject.toJSONString()); //announcementBoards
            }

            incrementTotalAnnouncs();

            send(new Response(true), outStream);
        }
    }
    
    private void read(Request request, boolean isGeneral, ObjectOutputStream outStream) {        

        if (!isGeneral && (request.getPublicKey() == null || request.getPublicKey().getEncoded().length != 294)) {
            send(new Response(false, -3), outStream);  //InvalidPublicKey

        } else if (!isGeneral &&(!userIdMap.containsKey(request.getPublicKey()))) {
            send(new Response(false, -1), outStream);  //UserNotRegistered

        } else if (request.getNumber() < 0) {
            send(new Response(false, -6), outStream);  //InvalidPostsNumber

        }
        
        // get list with file names in the specific directory
        String[] directoryList = getDirectoryList(request.getPublicKey());
        int directorySize = directoryList.length;


        if (request.getNumber() > directorySize) { //se for general o getDirectoryList ve o nr no general
            send(new Response(false, -10), outStream);  //TooMuchAnnouncements
        }

        else{
            String path = "./storage/";
            if(!isGeneral){
                System.out.println("READ method");
                int userId = userIdMap.get(request.getPublicKey());
                path += "AnnouncementBoards/" + userId + "/";
            }
            else{
                System.out.println("READGENERAL method");
                path += "GeneralBoard/";

            }

            int total;            
            if(request.getNumber() == 0) { //all posts
                total = directorySize;
            }
            else{
                total = request.getNumber();
            }

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
                send(new Response(true, announcementsToSend), outStream);
            } catch(Exception e){
                e.printStackTrace();
                send(new Response(false, -8), outStream);
            }
        }
    }
    
//////////////////////////////////////////
//										//
//           Auxiliary Methods          //
//    									//
//////////////////////////////////////////   
    
    private String[] getDirectoryList(PublicKey key){
        String path = "./storage/";
        if(key == null){
            path += "GeneralBoard/";
        }
        else{
            path += "AnnouncementBoards/" + userIdMap.get(key) + "/";
        }

        File file = new File(path);
        return file.list();
    }

    private boolean checkKey(PublicKey publicKey){ //checks if a key exists in the server keystore
        System.out.println("A que se esta a registar");
        System.out.println(publicKey.toString());
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);

            Enumeration aliases = ks.aliases();

            for (; aliases.hasMoreElements(); ) {

                String alias = (String) aliases.nextElement();
                System.out.println(alias);

                if (ks.isCertificateEntry(alias)) {
                    PublicKey key = ks.getCertificate(alias).getPublicKey();
                    System.out.println(key.toString());
                    if (key.equals(publicKey)) {
                        return true;
                    }
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void send(Response response, ObjectOutputStream outputStream){
        try {
            outputStream.writeObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void saveFile(String completePath, String announcement) throws IOException {
        byte[] bytesToStore = announcement.getBytes();
        try{
            File file = new File(completePath);
            FileOutputStream fos = new FileOutputStream(file);

            fos.write(bytesToStore);
            fos.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void newListener() {
        (new Thread(this)).start();
    }
    
/////////////////////////////////////////////
//   									   //
//  Method used to delete Tests' populate  //
//										   //
/////////////////////////////////////////////

    public void deleteUsers() throws IOException {

        System.out.println("Delete operation");

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
    
/////////////////////////////////////////////
//										   //
// Methods to save/get userIdMap from File //
//										   //
/////////////////////////////////////////////

    
    private void saveUserIdMap() {
        try {
            FileOutputStream fileOut = new FileOutputStream("./storage/UserIdMap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(userIdMap);
            out.close();
            fileOut.close();
            System.out.println("Serialized data of user ID Mapping is saved in ./storage/UserIdMap.ser");
         } catch (IOException i) {
            i.printStackTrace();
         }
    }
    
    private void getUserIdMap() {
        try {
           FileInputStream fileIn = new FileInputStream("./storage/UserIdMap.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           userIdMap = (Map<PublicKey, Integer>) in.readObject();
           in.close();
           fileIn.close();
        } catch (ClassNotFoundException c) {
           System.out.println("Map<PublicKey, Integer> class not found");
           c.printStackTrace();
           return;
        }
        catch(FileNotFoundException e){
            userIdMap = new HashMap<PublicKey, Integer>();
            saveUserIdMap();

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
            FileOutputStream fileOut = new FileOutputStream("./storage/TotalAnnouncements.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(TotalAnnouncements);
            out.close();
            fileOut.close();
            System.out.println("Serialized data of the total number of announcements is saved in ./storage/TotalAnnouncements.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    /*

    private void updateTotalAnnouncements(int updatedNumber) {
        TotalAnnouncements = updatedNumber;
        try {
            File file = new File("./storage/TotalAnnouncements.ser"); 
            file.delete();
            FileOutputStream fileOut = new FileOutputStream("./storage/TotalAnnouncements.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeInt(updatedNumber);
            out.close();
            fileOut.close();
            System.out.println("Total Number of announcements updated in ./storage/TotalAnnouncements.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }*/

    private void getTotalAnnouncementsFromFile() {
        try {
           FileInputStream fileIn = new FileInputStream("./storage/TotalAnnouncements.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           setTotalAnnouncements(in.readInt());
           in.close();
           fileIn.close();
        }
        catch(FileNotFoundException e){
            setTotalAnnouncements(0);
            saveTotalAnnouncements();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Total announcements-> " + TotalAnnouncements);
    }
}