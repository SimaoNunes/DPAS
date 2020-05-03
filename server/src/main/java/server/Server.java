package server;

import library.Envelope;
import library.Request;
import library.Response;

import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
	
	
	private ServerSocket server;
    private Map<PublicKey, String> userIdMap = null;
    private AtomicInteger totalAnnouncements;
    private CryptoManager cryptoManager = null;

    // File path strings
    private static final String STORAGE = "./storage/";
    private static final String STORAGE_GENERAL_BOARD = "./storage/generalboard";
    private static final String STORAGE_ANNOUNCEMENT_BOARDS = "./storage/announcementboards/";
    private static final String GENERAL_BOARD = "generalboard";
    private static final String ANNOUNCEMENT_BOARDS = "announcementboards/";
    private static final String USERMAP = "./storage/UserIdMap.ser";
    private static final String USERMAP_COPY = "./storage/UserIdMap_copy.ser";
    private static final String TOTAL_ANNOUNCEMENTS = "./storage/TotalAnnouncements.ser";
    private static final String TOTAL_ANNOUNCEMENTS_COPY = "./storage/TotalAnnouncements_copy.ser";
    private static String storagePath = "";
    private static String userMapPath = "";
    private static String announcementsPath = "";

    /********** Simulated Attacks Variables ***********/
    
    private boolean testFlag = false;
    private boolean dropNonceFlag = false;
    private boolean dropOperationFlag = false;
    private boolean handshake = false;
    private boolean integrityFlag = false;
    private Response oldResponse;
    private Envelope oldEnvelope;
    
    /**************************************************/

    protected Server(ServerSocket ss){

        server = ss;
        String serverPort = ss.getLocalPort() + "";  //adding "" converts int to string

        cryptoManager = new CryptoManager();
        oldResponse = new Response(cryptoManager.generateRandomNonce());
        oldEnvelope = new Envelope(oldResponse, null);

        // path variables
        storagePath       = "./storage/port_" + serverPort + "/";
        userMapPath       = storagePath + "UserIdMap.ser";
        announcementsPath = storagePath + "TotalAnnouncements.ser";
        String path       = storagePath + "generalboard/";
        File file = new File(path);
        file.mkdirs();
        
        getUserIdMap();
        getTotalAnnouncementsFromFile();
        
        System.out.println("Port: " + serverPort);
        newListener();
    }
    
//////////////////////////////////////////
//  								    //
//         Main method running          //
//    									//
//////////////////////////////////////////
    @SuppressWarnings("all")
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
                            cryptoManager.checkHash(envelope) &&
                            cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-2}))
                            {
                            register(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POST":
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            cryptoManager.checkHash(envelope) &&
                            cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-1, -4, -5})) 
                            {
                            post(envelope.getRequest(), false, outStream);
                        }
                        break;
                    case "POSTGENERAL":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            cryptoManager.checkHash(envelope) &&
                            cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-1, -4, -5}))
                            {
                            post(envelope.getRequest(), true, outStream);
                        }
                        break;
                    case "READ":
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                            cryptoManager.checkHash(envelope) &&
                            cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-3, -6, -10}))
                            {
                            read(envelope.getRequest(), false, outStream);
                        }
                        break;
                    case "READGENERAL":
                        if (checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) &&
                        	cryptoManager.checkHash(envelope) &&
                            cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getNonceServer()) &&
                            checkExceptions(envelope.getRequest(), outStream, new int[] {-6, -10}))
                        	{
                            read(envelope.getRequest(), true, outStream);
                        }
                        break;
                    case "NONCE":
                        handshake = true;
                        byte[] randomNonce = cryptoManager.generateRandomNonce(envelope.getRequest().getPublicKey());
                        if(!dropNonceFlag) {
                        	send(new Response(randomNonce), outStream);
                        } else {
                        	System.out.println("\nDROPEI\n");
                        }
                        handshake = false;
                        break;
                    case "DELETEALL":
                        deleteUsers();
                        break;
                    case "SHUTDOWN":
                        shutDown();
                        break;
                    case "TEST_FLAG_TRUE":
                        testFlag = true;
                        break;
                    case "TEST_FLAG_FALSE":
                        testFlag = false;
                        break;
                    case "INTEGRITY_FLAG_TRUE":
                        integrityFlag = true;
                        break;
                    case "INTEGRITY_FLAG_FALSE":
                        integrityFlag = false;
                        break;
                    case "DROP_NONCE_FLAG_TRUE":
                    	dropNonceFlag = true;
                    	break;
                    case "DROP_NONCE_FLAG_FALSE":
                    	dropNonceFlag = false;
                    	break;
                    case "DROP_OPERATION_FLAG_TRUE":
                    	dropOperationFlag = true;
                    	break;
                    case "DROP_OPERATION_FLAG_FALSE":
                    	dropOperationFlag = false;
                    	break;
                    default:
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

        synchronized (userIdMap) {
            String username = cryptoManager.checkKey(request.getPublicKey());
            System.out.println("REGISTER Method. Registering user: " + username);
            String path = storagePath + "announcementboards/" + username;
            File file = new File(path);
            file.mkdirs();
            userIdMap.put(request.getPublicKey(), username);
            saveUserIdMap();
            System.out.println("User " + username + " successfully registered!");
            if(!dropNonceFlag) {
            	send(new Response(true, request.getNonceClient()), outStream);
            } else {
            	System.out.println("\n\n\nDROPPED\n\n\n");
            }
        }
    }

    //////////////////////////////////////////////////
    //				      POST						//
    //////////////////////////////////////////////////
    
    private void post(Request request, Boolean general, ObjectOutputStream outStream){
        // Get userName from keystore
        String username = userIdMap.get(request.getPublicKey());
        String path = storagePath + "announcementboards/" + username + "/";        
        // Write to file
        JSONObject announcementObject =  new JSONObject();
        announcementObject.put("id", Integer.toString(getTotalAnnouncements()));
        announcementObject.put("user", username);
        announcementObject.put("message", request.getMessage());
        
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy 'at' HH:mm");
        announcementObject.put("date", ft.format(dNow).toString());

        int[] refAnnouncements = request.getAnnouncements();
        
        if(refAnnouncements != null){
            JSONArray annoucementsList = new JSONArray();
            for(int i = 0; i < refAnnouncements.length; i++){
                annoucementsList.add(Integer.toString(refAnnouncements[i]));
            }
            announcementObject.put("ref_announcements", annoucementsList);
        }

        if(general){
            path = storagePath + "generalboard/";
        }

        try {
            saveFile(path + Integer.toString(getTotalAnnouncements()), announcementObject.toJSONString()); //GeneralBoard
        } catch (IOException e) {
            send(new Response(false, -9, request.getNonceClient()), outStream);
        }

        incrementTotalAnnouncs();
        saveTotalAnnouncements();
        if(!dropOperationFlag) {
        	send(new Response(true, request.getNonceClient()), outStream);
        } else {
        	System.out.println("\n\n\nDROPPED\n\n\n");
        }
    }
    
    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////
    
    private void read(Request request, boolean isGeneral, ObjectOutputStream outStream) {

        String[] directoryList = getDirectoryList(request.getPublicKeyToReadFrom());
        int directorySize = directoryList.length;

        String path = storagePath;
        if(!isGeneral) {
            System.out.println("READ method");
            String username = userIdMap.get(request.getPublicKeyToReadFrom());
            path += ANNOUNCEMENT_BOARDS + username + "/";
        } else {
            System.out.println("READGENERAL method");
            path += GENERAL_BOARD;
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
                fileToRead = directoryList[directorySize-1];
                announcement = (JSONObject) parser.parse(new FileReader(path + fileToRead));
                directorySize--;
                annoucementsList.add(announcement);
            }
            JSONObject announcementsToSend =  new JSONObject();
            announcementsToSend.put("announcementList", annoucementsList);
            if(!dropOperationFlag) {
                send(new Response(true, announcementsToSend, request.getNonceClient()), outStream);
            } else {
            	System.out.println("\nDROPPED\n");
            }
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
        String path = storagePath;
        if(key == null) {
            path += GENERAL_BOARD;
        }
        else {
            path += ANNOUNCEMENT_BOARDS + userIdMap.get(key) + "/";
        }

        File file = new File(path);
        return file.list();
    }

    private void send(Response response, ObjectOutputStream outputStream){
        try {
        	// Sign response
            byte[] finalBytes = cryptoManager.cipher(response, cryptoManager.getPrivateKey());
            // SIMULATE ATTACKER: changing an attribute from the response will make it different from the hash]
            if(integrityFlag) {
                response.setSuccess(false);
                response.setErrorCode(-33);
            }
            // SIMULATE ATTACKER: Replay attack by sending a replayed message from the past (this message is simulated)]
            if(testFlag && !handshake){
                outputStream.writeObject(oldEnvelope);
            }
            else{
                outputStream.writeObject(new Envelope(response, finalBytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    private void saveFile(String completePath, String announcement) throws IOException {
        byte[] bytesToStore = announcement.getBytes();
        File file = new File(completePath);

        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytesToStore);

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

        String path = storagePath + "announcementboards";

        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();

        path = storagePath + "generalboard";

        FileUtils.deleteDirectory(new File(path));
        files = new File(path);
        files.mkdirs();

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
        String copyFileName = "UserIdMap_copy.ser";
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storagePath + copyFileName))) {
            out.writeObject(userIdMap);
            System.out.println("Created updated copy of the userIdMap");

            File original = new File(userMapPath);
            File copy = new File(storagePath + copyFileName);

            if(original.delete()){
                copy.renameTo(original);
            }

         } catch (IOException i) {
            i.printStackTrace();
         }
    }
    
    private void getUserIdMap() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(userMapPath))) {
           userIdMap = (Map<PublicKey, String>) in.readObject();
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

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userMapPath))) {
            out.writeObject(userIdMap);

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
        totalAnnouncements.incrementAndGet();
    }

    private void setTotalAnnouncements(int value){
        totalAnnouncements.set(value);
    }

    private int getTotalAnnouncements(){
        return totalAnnouncements.get();
    }

    private void saveTotalAnnouncements(){
        String copyFileName = "TotalAnnouncements_copy.ser";
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storagePath + copyFileName))) {
            out.writeObject(totalAnnouncements.get());
            System.out.println("Serialized data saved in copy");

            File original = new File(announcementsPath);
            File copy = new File(storagePath + copyFileName);

            if(original.delete()){
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void getTotalAnnouncementsFromFile() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(announcementsPath))) {
           int a = (int) in.readObject();
           System.out.println(a);
           totalAnnouncements = new AtomicInteger(a);
        }
        catch(FileNotFoundException e){
            totalAnnouncements = new AtomicInteger(0);
            createOriginalAnnouncs();
        } catch (
            IOException | 
            ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Total announcements-> " + totalAnnouncements);
    }

    private void createOriginalAnnouncs(){

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(announcementsPath))) {
            out.writeObject(totalAnnouncements.get());
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    //////////////////////////////////////////
    //  									//
    //          Check exceptions            //
    //	                                    //
    //////////////////////////////////////////
    @SuppressWarnings("all")
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
                    if (request.getPublicKey() == null || cryptoManager.checkKey(request.getPublicKey()) == "" || (request.getOperation().equals("READ") && (request.getPublicKeyToReadFrom() == null || cryptoManager.checkKey(request.getPublicKeyToReadFrom()) == ""))) {
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

                default:
                    break;
            }
        }
        return true;
    }
    
}
