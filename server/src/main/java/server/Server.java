package server;

import exceptions.IntegrityException;
import exceptions.NonceTimeoutException;
import library.Envelope;
import library.Pair;
import library.Request;
import library.Response;

import org.apache.commons.io.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
	
	
    private ServerSocket server;
    private String serverPort;
    private ConcurrentHashMap<PublicKey, String> userIdMap = null;
    private AtomicInteger totalAnnouncements;
    private CryptoManager cryptoManager = null;

    // File path strings
    private String storagePath = "";
    private String userMapPath = "";
    private String userMapPathCopy = "";
    private String totalAnnouncementsPath = "";
    private String totalAnnouncementsPathCopy = "";
    private String announcementBoardsPath = "";
    private String announcementBoardsFile = "";
    private String announcementBoardsCopy = "";
    private String generalBoardPath = "";
    private String generalBoardFile = "";
    private String generalBoardCopy = "";

    /********** Simulated Attacks Variables ***********/
    
    private boolean replayFlag = false;
    private boolean dropNonceFlag = false;
    private boolean dropOperationFlag = false;
    private boolean handshake = false;
    private boolean integrityFlag = false;
    private Response oldResponse;
    private Envelope oldEnvelope;
    
    /**************************************************/
    /********************** Regular Register **********************/

    private Pair<Integer, GeneralBoard> generalBoard;

    
    /***************** Atomic Register variables ******************/
    
    private ConcurrentHashMap<PublicKey, Pair<Integer, AnnouncementBoard>> usersBoards = null;
    private ConcurrentHashMap<PublicKey, ConcurrentHashMap<PublicKey, Pair<Integer, Integer>>> listening = null;
    //<PubKey reading from < pubKey who is reading, <Rid, nPosts to read>>>
    /**************************************************************/


    protected Server(ServerSocket ss, int port) {

        server            = ss;
        serverPort		  = port + "";  //adding "" converts int to string

        cryptoManager = new CryptoManager(port);
        oldResponse   = new Response(cryptoManager.generateRandomNonce());
        oldEnvelope   = new Envelope(oldResponse, null);
        
        // Path variables
        storagePath       		   = "./storage/port_" + serverPort + "/";
        userMapPath       		   = storagePath + "UserIdMap.ser";
        userMapPathCopy			   = storagePath + "UserIdMap_copy.ser";
        totalAnnouncementsPath	   = storagePath + "TotalAnnouncements.ser";
        totalAnnouncementsPathCopy = storagePath + "TotalAnnouncements_copy.ser";
        announcementBoardsPath	   = storagePath + "announcementboards/";
        announcementBoardsFile     = announcementBoardsPath + "announcementBoards.ser";
        announcementBoardsCopy     = announcementBoardsPath + "announcementBoards_copy.ser";
        generalBoardPath		   = storagePath + "generalboard/";
        generalBoardFile           = generalBoardPath + "generalBoard.ser";
        generalBoardCopy           = generalBoardPath + "generalBoard_copy.ser";


        File gb = new File(generalBoardPath);
        gb.mkdirs();

        File ab = new File(announcementBoardsPath);
        ab.mkdirs();

        getGeneralBoard();
        getUsersBoards();
        listening = new ConcurrentHashMap<>();

        System.out.println("SERVER ON PORT " + this.serverPort + ": Up and running.");
        getUserIdMap();
        getTotalAnnouncementsFromFile();

        //initUsersBoard();

        newListener();
    }
    
    
//////////////////////////////////////////
//
//         Main method running
//
//////////////////////////////////////////
    @SuppressWarnings("all")
    public void run() {

        Socket socket = null;
        ObjectOutputStream outStream;
        ObjectInputStream inStream;

        try {
            socket = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        newListener();

        try {
            inStream = new ObjectInputStream(socket.getInputStream());
            outStream = new ObjectOutputStream(socket.getOutputStream());
            try {
                Envelope envelope = (Envelope) inStream.readObject();
                switch(envelope.getRequest().getOperation()) {
                    case "REGISTER":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7}) && 
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(envelope.getRequest().getUsername())) &&
                            	cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                            	checkExceptions(envelope.getRequest(), outStream, new int[] {-2}))
                        {
                            register(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POST":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                            	cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                            	checkExceptions(envelope.getRequest(), outStream, new int[] {-4, -5})) 
                        {
                            write(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POSTGENERAL":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) && 
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-4, -5}))
                        {
                        	writeGeneral(envelope.getRequest(), outStream);
                        }
                        break;
                    case "READ":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) && 
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            read(envelope.getRequest(), outStream);
                        }
                        break;
                    case "READGENERAL":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-6, -10}))
                        {
                            readGeneral(envelope.getRequest());
                        }
                        break;
                    case "READCOMPLETE":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-3}))
                        {
                            readComplete(envelope.getRequest());
                        }
                        break;
                    case "NONCE":
                    	if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), envelope.getRequest().getPublicKey())) {
                    		handshake = true;
	                        cryptoManager.generateRandomNonce(envelope.getRequest().getPublicKey());
	                        if(!dropNonceFlag) {
	                        	sendResponse(new Response(cryptoManager.getServerNonce(envelope.getRequest().getPublicKey())), outStream);
	                        } else {
	                        	System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED NONCE");
	                        }
	                        handshake = false;
                    	}
                        break;
                    case "WTS":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            wtsRequest(envelope.getRequest(), false,  outStream);
                        }
                    	break;
                    case "WTSGENERAL":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            wtsRequest(envelope.getRequest(), true, outStream);
                        }
                        break;

                    case "DELETEALL":
                        deleteUsers();
                        break;
                    case "SHUTDOWN":
                        shutDown();
                        break;
                    case "REPLAY_FLAG_TRUE":
                        replayFlag = true;
                        break;
                    case "REPLAY_FLAG_FALSE":
                        replayFlag = false;
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
//
//             API Methods
//
//////////////////////////////////////////

    //////////////////////////////////////////////////
    //				    REGISTER
    //////////////////////////////////////////////////
    
    public void register(Request request, ObjectOutputStream outStream) {
        System.out.println("SERVER ON PORT " + this.serverPort + ": REGISTER METHOD");
        String username = cryptoManager.checkKey(request.getPublicKey());
        String path = announcementBoardsPath + username;
        File file = new File(path);
        file.mkdirs();
        userIdMap.put(request.getPublicKey(), username);
        saveUserIdMap();
        usersBoards.put(request.getPublicKey(), new Pair<>(0, new AnnouncementBoard(request.getUsername())));
        saveUsersBoards();
        if(!dropOperationFlag) {
            sendResponse(new Response(true, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
        } else {
            System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED REGISTER");
        }
    }

    
    //////////////////////////////////////////////////
    //				      POST
    //////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	private void write(Request request, ObjectOutputStream outStream) throws IntegrityException, NonceTimeoutException {
        // Get userName from keystore
        if(request.getTs() > usersBoards.get(request.getPublicKey()).getFirst()) {  // if ts' > ts then (ts, val) := (ts', v')
            usersBoards.get(request.getPublicKey()).setFirst(request.getTs());  // ts = ts'

            String username = userIdMap.get(request.getPublicKey());                    //val = v'
            String path = announcementBoardsPath + username + "/";
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
            System.out.println("aqui tou a guardar");
            usersBoards.get(request.getPublicKey()).getSecond().addAnnouncement(announcementObject); //update val with the new post
            saveUsersBoards();
            System.out.println(usersBoards.get(request.getPublicKey()).getSecond().size());
            /*try {
                saveFile(path + Integer.toString(getTotalAnnouncements()), announcementObject.toJSONString());
            } catch (IOException e) {
                sendResponse(new Response(false, -9, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
            }

            incrementTotalAnnouncs();
            saveTotalAnnouncements();*/

        }
        if(listening.contains(userIdMap.get(request.getPublicKey()))) { // no one is reading from who is writing
            for(Map.Entry<PublicKey, Pair<Integer, Integer>> entry : listening.get(request.getPublicKey()).entrySet()){  //for every listening[q]
                byte[] nonce = null;
                try {
                    nonce = startOneWayHandshake(userIdMap.get(entry.getKey()));
                } catch (NonceTimeoutException e) {
                    e.printStackTrace();
                } catch (IntegrityException e) {
                    e.printStackTrace();
                }
                // -----> One way Handshake
                int port = getClientPort(userIdMap.get(entry.getKey()));
                try(ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", port).getOutputStream())) {
                    int rid = entry.getValue().getFirst();
                    int number = entry.getValue().getSecond();
                    int ts = usersBoards.get(entry.getKey()).getFirst();
                    JSONObject objectToSend = new JSONObject();
                    objectToSend.put("announcementList", usersBoards.get(entry.getKey()).getSecond().getAnnouncements(number));

                    sendRequest(new Request("VALUE", rid, ts, nonce, objectToSend, cryptoManager.getPublicKeyFromKs("server")), outputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //send
            }
        }

        if(!dropOperationFlag) {
            sendResponse(new Response(true, request.getClientNonce(), usersBoards.get(request.getPublicKey()).getFirst(), cryptoManager.getPublicKeyFromKs("server")), outStream);
        } else {
            System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED POST");
        }

    }

    
    //////////////////////////////////////////////////
    //				   POST GENERAL
    //////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	private void writeGeneral(Request request, ObjectOutputStream outStream) {
        System.out.println("WRITEGENERALMETHOD");
        if(request.getTs() > generalBoard.getFirst()){
            generalBoard.setFirst(request.getTs());

            // Get userName from keystore
            String username = userIdMap.get(request.getPublicKey());
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

            generalBoard.getSecond().addAnnouncement(announcementObject, request.getSignature());

            saveGeneralBoard();

            /*String path = generalBoardPath;

            try {
                saveFile(path + request.getTs(), announcementObject.toJSONString()); //GeneralBoard
            } catch (IOException e) {
                sendResponse(new Response(false, -9, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
            }

            incrementTotalAnnouncs();
            saveTotalAnnouncements();*/
        }

        if(!dropOperationFlag) {
            sendResponse(new Response(true, request.getClientNonce(), generalBoard.getFirst(), cryptoManager.getPublicKeyFromKs("server")), outStream);
        } else {
            System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED POST");
        }
    }
    
    
    //////////////////////////////////////////////////
    //				      READ
    //////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	private void read(Request request, ObjectOutputStream outStream) {

        if(listening.contains(request.getPublicKeyToReadFrom())) {	//someone is already reading that board
            listening.get(request.getPublicKeyToReadFrom()).put(request.getPublicKey(), new Pair<>(request.getRid(), request.getNumber()));  //listening [p] := r ;
        }
        else {
            listening.put(request.getPublicKeyToReadFrom(), new ConcurrentHashMap<>()); //listening [p] := r ;
            listening.get(request.getPublicKeyToReadFrom()).put(request.getPublicKey(), new Pair<>(request.getRid(), request.getNumber()));
        }

        String[] directoryList = getDirectoryList(request.getPublicKeyToReadFrom());
        //int directorySize = directoryList.length;

        System.out.println("SERVER ON PORT " + this.serverPort + ": READ METHOD");
        String username = userIdMap.get(request.getPublicKeyToReadFrom());
        String path = announcementBoardsPath + username + "/";

        int total = request.getNumber();

        //Arrays.sort(directoryList);
        //JSONParser parser = new JSONParser();
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", getClientPort(userIdMap.get(request.getPublicKey()))).getOutputStream())){
            /*JSONArray annoucementsList = new JSONArray();
            JSONObject announcement;

            String fileToRead;
            for (int i=0; i<total; i++) {
                fileToRead = directoryList[directorySize-1];
                announcement = (JSONObject) parser.parse(new FileReader(path + fileToRead));
                directorySize--;
                annoucementsList.add(announcement);
            }
            */
            JSONObject announcementsToSend =  new JSONObject();
            announcementsToSend.put("announcementList", usersBoards.get(request.getPublicKeyToReadFrom()).getSecond().getAnnouncements(total));
            // if(!dropOperationFlag) {
            //     // -----> Handshake one way
            //     //send(new Response(true, announcementsToSend, request.getNonceClient(), request.getRid()), outStream);
            // } else {
            // 	System.out.println("DROPPED READ");
            // } 

            // Send response to client
            // ------> Handshake one way
            byte[] nonce = startOneWayHandshake(userIdMap.get(request.getPublicKey()));
            
            sendRequest(new Request("VALUE", request.getRid(), usersBoards.get(request.getPublicKeyToReadFrom()).getFirst(), nonce, announcementsToSend, cryptoManager.getPublicKeyFromKs("server")), outputStream);

        } catch(Exception e) {
            e.printStackTrace();
            sendResponse(new Response(false, -8, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
        }
    }
    

    //////////////////////////////////////////////////
    //				   READ GENERAL
    //////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	private void readGeneral(Request request) {

        String path = "";
        System.out.println("SERVER ON PORT " + this.serverPort + ": READGENERAL method");
        path = generalBoardPath;

        int total;
        if(request.getNumber() == 0) { //all posts
            total = generalBoard.getSecond().size();    //directorySize
        } else {
            total = request.getNumber();
        }
        JSONObject announcementsToSend = generalBoard.getSecond().getAnnouncements(total);
        try(ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", getClientPort(userIdMap.get(request.getPublicKey()))).getOutputStream())) {
            if(!dropOperationFlag) {
                byte[] nonce = startOneWayHandshake(userIdMap.get(request.getPublicKey()));
                sendRequest(new Request("VALUEGENERAL", request.getRid(), generalBoard.getFirst(), nonce, announcementsToSend, cryptoManager.getPublicKeyFromKs("server")), outputStream);
            } else {
                System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED READ GENERAL");
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (IntegrityException e) {
            e.printStackTrace();
        } catch (NonceTimeoutException e) {
            e.printStackTrace();
        }
        /*
        Arrays.sort(directoryList);
        JSONParser parser = new JSONParser();
        try {
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
			    // send(new Response(true, announcementsToSend, request.getNonceClient()), outStream);   FIXME-> enviar o rid?
			} else {
			    System.out.println("SERVER ON PORT " + this.serverPort + ": DROPPED READ GENERAL");
			}
		} catch (IOException | ParseException e) {
			sendResponse(new Response(false, -8, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
		}*/
    }

    private void readComplete(Request request) {
        if(request.getRid() == listening.get(request.getPublicKeyToReadFrom()).get(request.getPublicKey()).getFirst()) {
            listening.get(request.getPublicKeyToReadFrom()).remove(request.getPublicKey());
        }
    }
    
    
    //////////////////////////////////////////////
    //				   SEND WTS
    //////////////////////////////////////////////
    
    private void wtsRequest(Request request, boolean isGeneral, ObjectOutputStream outStream) {
    	System.out.println("SERVER ON PORT " + this.serverPort + ": WTS METHOD");
    	int wts = 0;
    	if(isGeneral){
            wts = generalBoard.getFirst();
        }
    	else{
            wts = usersBoards.get(request.getPublicKey()).getFirst();
        }
        sendResponse(new Response(true, request.getClientNonce(), wts, cryptoManager.getPublicKeyFromKs("server")), outStream);
    }
    
    
//////////////////////////////////////////
//
//           Auxiliary Methods
//
//////////////////////////////////////////

    private Boolean checkValidAnnouncements(int[] announcs) {
        int total = getTotalAnnouncements();
        for (int i = 0; i < announcs.length; i++) { 		      
            if (announcs[i] >= total ) {
                return false;
            }		
        } 	
        return true;
    }
    
    private String[] getDirectoryList(PublicKey key){
        String path = "";
        if(key == null) {
            path = generalBoardPath;
        } else {
            path = announcementBoardsPath + userIdMap.get(key) + "/";
        }

        File file = new File(path);
        return file.list();
    }

    private void sendResponse(Response response, ObjectOutputStream outputStream) {
        try {
        	// Sign response
            byte[] signature = cryptoManager.signResponse(response);
            /***** SIMULATE ATTACKER: changing an attribute from the response will make it different from the hash] *****/
            if(integrityFlag) {
                response.setSuccess(false);
                response.setErrorCode(-33);
            }
            /************************************************************************************************************/
            /***** SIMULATE ATTACKER: Replay attack by sending a replayed message from the past (this message is simulated)] *****/
            if(replayFlag && !handshake){
                outputStream.writeObject(oldEnvelope);
            }
            /*********************************************************************************************************************/
            else{
                outputStream.writeObject(new Envelope(response, signature));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    private void sendRequest(Request request, ObjectOutputStream outputStream){
        try {
            // Sign response
            byte[] signature = cryptoManager.signRequest(request);
            /***** SIMULATE ATTACKER: changing an attribute from the response will make it different from the hash] *****/
            if(integrityFlag) {
                //request.setSuccess(false);  --> alteramos outras coisas
                //request.setErrorCode(-33);
            }
            /************************************************************************************************************/
            /***** SIMULATE ATTACKER: Replay attack by sending a replayed message from the past (this message is simulated)] *****/
            if(replayFlag && !handshake){
                outputStream.writeObject(oldEnvelope);
            }
            /*********************************************************************************************************************/
            else{
                outputStream.writeObject(new Envelope(request, signature));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Envelope sendReceive(Request serverRequest, ObjectOutputStream outputStream, ObjectInputStream inputStream) {
        Envelope envelope = null;
        try {
        	// Sign request
            byte[] signature = cryptoManager.signRequest(serverRequest);
            outputStream.writeObject(new Envelope(serverRequest, signature));
            // exceptions de timeout e o crl (nonce timeout)   FIXME -> falta adicionar as exceptions
            return (Envelope) inputStream.readObject();
        } catch (IOException | 
                ClassNotFoundException e) {
            e.printStackTrace();
        }
        return envelope; 
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
//
//  Method used to delete Tests' populate && Shut down Server && Start server
//
////////////////////////////////////////////////////////////////////////////////

    public void deleteUsers() throws IOException {

        System.out.println("SERVER ON PORT " + this.serverPort + ": DELETE OPERATION");

        userIdMap.clear();
        saveUserIdMap();

        String path = announcementBoardsPath;

        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();

        path = generalBoardPath;

        FileUtils.deleteDirectory(new File(path));
        files = new File(path);
        files.mkdirs();

        setTotalAnnouncements(0);
        saveTotalAnnouncements();
    }

    private void shutDown(){
    	System.out.println("SERVER ON PORT " + this.serverPort + ": SHUTDOWN OPERATION");
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name.split("@")[0]);

        try {
            Runtime.getRuntime().exec("kill -SIGINT " + name.split("@")[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
/////////////////////////////////////////////
//
//     Method to initialize user pairs
//
/////////////////////////////////////////////
    /*
    @SuppressWarnings("unchecked")
	private void initUsersBoard() {

        usersBoards = new ConcurrentHashMap<PublicKey, Pair<Integer, AnnouncementBoard>>();

        String[] users = new File(announcementBoardsPath).list();
        if(users != null){
            for(String user : users){
                String path = announcementBoardsPath + '/' + user;
                String[] postsFromUser = new File(path).list();
                try{

                    JSONParser parser = new JSONParser();
                    JSONArray annoucementsList = new JSONArray();

                    for (String file : postsFromUser) {
                        annoucementsList.add((JSONObject) parser.parse(new FileReader(path + '/' + file)));
                    }

                    int timestamp = postsFromUser.length;
                    AnnouncementBoard ab = new AnnouncementBoard(user, annoucementsList);
                    Pair<Integer, AnnouncementBoard> pair = new Pair<Integer, AnnouncementBoard>(timestamp, ab);
                    usersBoards.put(user, pair);

                } catch(Exception e) {
                    System.out.println("OPAAAA");
                }
            }
        }
        
    }*/
    
    
/////////////////////////////////////////////
//
// Methods to save/get userIdMap from File
//
/////////////////////////////////////////////
    
    private void saveUserIdMap() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(userMapPathCopy))) {
            out.writeObject(userIdMap);
            System.out.println("SERVER ON PORT " + this.serverPort + ": Created updated copy of the userIdMap");

            File original = new File(userMapPath);
            File copy = new File(userMapPathCopy);

            if(original.delete()){
                copy.renameTo(original);
            }

         } catch (IOException i) {
            i.printStackTrace();
         }
    }
    
    @SuppressWarnings("unchecked")
	private void getUserIdMap() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(userMapPath))) {
           userIdMap = (ConcurrentHashMap<PublicKey, String>) in.readObject();
        } catch (ClassNotFoundException c) {
           System.out.println("SERVER ON PORT " + this.serverPort + ": Map<PublicKey, String> class not found");
           c.printStackTrace();
           return;
        }
        catch(FileNotFoundException e){
            userIdMap = new ConcurrentHashMap<PublicKey, String>();
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

/////////////////////////////////////////////
//
// Methods to save/get Announcement Boards from File
//
/////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private void saveUsersBoards() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(announcementBoardsCopy))) {
            out.writeObject(usersBoards);
            System.out.println("SERVER ON PORT " + this.serverPort + ": Created updated copy of Announcement Boards");

            File original = new File(announcementBoardsFile);
            File copy = new File(announcementBoardsCopy);

            if(original.delete()){
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void getUsersBoards() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(announcementBoardsPath))) {
            usersBoards = (ConcurrentHashMap<PublicKey, Pair<Integer, AnnouncementBoard>>) in.readObject();
        } catch (ClassNotFoundException c) {
            System.out.println("SERVER ON PORT " + this.serverPort + ": ConcurrentHashMap<PublicKey, Pair<Integer, AnnouncementBoard>> class not found");
            c.printStackTrace();
            return;
        }
        catch(FileNotFoundException e){
            usersBoards = new ConcurrentHashMap<>();
            createOriginalUsersBoards();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOriginalUsersBoards() {

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(announcementBoardsFile))) {
            out.writeObject(usersBoards);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

/////////////////////////////////////////////
//
// Methods to save/get General Board from File
//
/////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private void saveGeneralBoard() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(generalBoardCopy))) {
            out.writeObject(generalBoard);
            System.out.println("SERVER ON PORT " + this.serverPort + ": Created updated copy of the General Board");

            File original = new File(generalBoardFile);
            File copy = new File(generalBoardCopy);

            if(original.delete()){
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void getGeneralBoard() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(generalBoardFile))) {
            generalBoard = (Pair<Integer, GeneralBoard>) in.readObject();
        } catch (ClassNotFoundException c) {
            System.out.println("SERVER ON PORT " + this.serverPort + ": Pair<Integer, GeneralBoard> class not found");
            c.printStackTrace();
            return;
        }
        catch(FileNotFoundException e){
            generalBoard = new Pair<>(0, new GeneralBoard());
            createOriginalGeneralBoard();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOriginalGeneralBoard() {

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(generalBoardFile))) {
            out.writeObject(generalBoard);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    
/////////////////////////////////////////////////////////
//
// Methods to get/update total announcements from File
//
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
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(totalAnnouncementsPathCopy))) {
            out.writeObject(totalAnnouncements.get());
            System.out.println("SERVER ON PORT " + this.serverPort + ": Serialized data saved in copy");

            File original = new File(totalAnnouncementsPath);
            File copy = new File(totalAnnouncementsPathCopy);

            if(original.delete()){
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void getTotalAnnouncementsFromFile() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(totalAnnouncementsPath))) {
           int a = (int) in.readObject();
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
        System.out.println("SERVER ON PORT " + this.serverPort + ": Total announcements-> " + totalAnnouncements);
    }

    private void createOriginalAnnouncs() {

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(totalAnnouncementsPath))) {
            out.writeObject(totalAnnouncements.get());
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

//////////////////////////////////////////
//
//          Check exception
//
//////////////////////////////////////////
    @SuppressWarnings("all")
    public boolean checkExceptions(Request request, ObjectOutputStream outStream, int[] codes) {
        for (int i = 0; i < codes.length; i++) {
            switch(codes[i]) {
                // ## UserNotRegistered ## -> check if user is registered
                case -1:
                    if(!userIdMap.containsKey(request.getPublicKey())) {
                        sendResponse(new Response(false, -1, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;
                    }
                    break;
                // ## AlreadyRegistered ## -> check if user is already registered
                case -2:
                    if (userIdMap.containsKey(request.getPublicKey())) {
                        sendResponse(new Response(false, -2, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;
                    }
                    break;
                // ## UserNotRegistered ## -> [READ] check if user TO READ FROM is registered
                case -3:
                    if(!userIdMap.containsKey(request.getPublicKeyToReadFrom())) {
                        sendResponse(new Response(false, -3, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;
                    }
                    break;
                // ## MessageTooBig ## -> Check if message length exceeds 255 characters
                case -4:
                    if(request.getMessage().length() > 255) {
                        sendResponse(new Response(false, -4, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;
                    }
                    break;
                // ## InvalidAnnouncement ## -> checks if announcements referred by the user are valid
                case -5:
                    if(request.getAnnouncements() != null && !checkValidAnnouncements(request.getAnnouncements())) {
                        sendResponse(new Response(false, -5, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;      
                    }
                    break;
                // ## InvalidPostsNumber ## -> check if number of requested posts are bigger than zero
                case -6:
                    if (request.getNumber() < 0) {
                        sendExceptionCode(request.getPublicKey(), request.getClientNonce(), -6);
                        return false;
                    }
                    break;
                // ## UnknownPublicKey ## -> Check if key is null or unknown by the Server. If method is read, check if key to read from is null
                case -7:
                    if (request.getPublicKey() == null || cryptoManager.checkKey(request.getPublicKey()) == "" || (request.getOperation().equals("READ") && (request.getPublicKeyToReadFrom() == null || cryptoManager.checkKey(request.getPublicKeyToReadFrom()) == ""))) {
                    	sendResponse(new Response(false, -7, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server")), outStream);
                        return false;
                    }
                    break;
                // ## TooMuchAnnouncements ## -> Check if user is trying to read more announcements that Board number of announcements
                case -10:
                    if ((request.getOperation().equals("READ") && request.getNumber() > getDirectoryList(request.getPublicKey()).length) || (request.getOperation().equals("READGENERAL") && request.getNumber() > getDirectoryList(null).length) ) {
                        sendExceptionCode(request.getPublicKey(), request.getClientNonce(), -10);
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }
    

    private void sendExceptionCode(PublicKey clientKey, byte[] clientNonce, int code){
        int clientPort = getClientPort(userIdMap.get(clientKey));
        int ts = usersBoards.get(clientKey).getFirst();
        try(ObjectOutputStream newOutputStream = new ObjectOutputStream(new Socket("localhost", clientPort).getOutputStream())) {
            sendResponse(new Response(false, code, clientNonce, cryptoManager.getPublicKeyFromKs("server"), ts), newOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getClientPort(String client){
        try(BufferedReader reader = new BufferedReader(new FileReader("../clients_addresses.txt"))){
            String line;
            while((line = reader.readLine()) != null){
                String[] splitted = line.split(":");
                if(splitted[0].equals(client)){
                    return Integer.parseInt(splitted[2]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private byte[] startOneWayHandshake(String username) throws NonceTimeoutException, IntegrityException {
        Envelope nonceEnvelope = askForClientNonce(cryptoManager.getPublicKeyFromKs("server"), getClientPort(username));
        if(cryptoManager.verifyResponse(nonceEnvelope.getResponse(), nonceEnvelope.getSignature(), userIdMap.get(nonceEnvelope.getResponse().getPublicKey()))) {
            return nonceEnvelope.getResponse().getNonce();
        } else {
            throw new IntegrityException("Integrity Exception");
        }
    }

    private Envelope askForClientNonce(PublicKey serverKey, int port) throws NonceTimeoutException {
        try(Socket socket = new Socket("localhost", port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            return sendReceive(new Request("NONCE", serverKey), outputStream, inputStream);
        } catch (IOException e) {
            throw new NonceTimeoutException("The operation was not possible, please try again!"); //IOException apanha tudo
        }
    }
    
}