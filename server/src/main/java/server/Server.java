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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
	
	private int nServers = 4;
	private int nFaults = 1;
	private int nQuorum = 2;
	private static final int PORT = 9000;
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
    private String announcementBoardsPathCopy = "";
    private String generalBoardPath = "";
    private String generalBoardPathCopy = "";

    /********** Simulated Attacks Variables ***********/
    
    private boolean replayFlag = false;
    private boolean dropNonceFlag = false;
    private boolean dropOperationFlag = false;
    private boolean handshake = false;
    private boolean integrityFlag = false;
    private boolean atomicWriteFlag = false;
    private boolean concurrentWrite = false;
    private Response oldResponse;
    private Envelope oldEnvelope;
    
    /**************************************************/

    /******************** AUTHENTICATED DOUBLE ECHO VARIABLES ******/
    private ConcurrentHashMap<PublicKey, Boolean> sentEcho;
    private ConcurrentHashMap<PublicKey, Boolean> sentReady;
    private ConcurrentHashMap<PublicKey, Boolean> delivered;
    private ConcurrentHashMap<PublicKey, ConcurrentHashMap<PublicKey, Envelope>> echos;
    private ConcurrentHashMap<PublicKey, ConcurrentHashMap<PublicKey, Envelope>> readys;


    
    /********************** Regular Register **********************/

    private Pair<Integer, GeneralBoard> generalBoard;

    /***************** Atomic Register variables ******************/
    
    private ConcurrentHashMap<PublicKey, Pair<Integer, AnnouncementBoard>> usersBoards = null;
    private ConcurrentHashMap<PublicKey, ConcurrentHashMap<PublicKey, Pair<Integer, Integer>>> listening = null;
    //<PubKey reading from < pubKey who is reading, <Rid, nPosts to read>>>
    /**************************************************************/


    protected Server(ServerSocket ss, int port) {

        server = ss;
        serverPort = port + "";  //adding "" converts int to string

        cryptoManager = new CryptoManager(port);
        oldResponse   = new Response(cryptoManager.generateRandomNonce());
        oldEnvelope   = new Envelope(oldResponse, null);
        
        // Path variables
        storagePath       		   = "./storage/port_" + serverPort + "/";
        userMapPath       		   = storagePath + "UserIdMap.ser";
        userMapPathCopy			   = storagePath + "UserIdMap_copy.ser";
        totalAnnouncementsPath	   = storagePath + "TotalAnnouncements.ser";
        totalAnnouncementsPathCopy = storagePath + "TotalAnnouncements_copy.ser";
        announcementBoardsPath     = storagePath + "AnnouncementBoards.ser";
        announcementBoardsPathCopy = storagePath + "AnnouncementBoards_copy.ser";
        generalBoardPath           = storagePath + "GeneralBoard.ser";
        generalBoardPathCopy	   = storagePath + "GeneralBoard_copy.ser";

        File storage = new File(storagePath);
        storage.mkdirs();
        
        getGeneralBoard();
        
        getUsersBoards();

        listening = new ConcurrentHashMap<PublicKey, ConcurrentHashMap<PublicKey, Pair<Integer, Integer>>>();

        sentEcho = new ConcurrentHashMap<>();
        sentReady = new ConcurrentHashMap<>();
        delivered = new ConcurrentHashMap<>();
        echos = new ConcurrentHashMap<>();
        readys = new ConcurrentHashMap<>();

        getUserIdMap();

        getTotalAnnouncementsFromFile();
        
        System.out.println("SERVER ON PORT " + this.serverPort + ": Up and running.");

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
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(envelope.getRequest().getUsername())) &&
                            	cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                            	checkExceptions(envelope.getRequest(), outStream, new int[] {-2}, null))
                        {
                            register(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POST":
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                            	cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                            	checkExceptions(envelope.getRequest(), outStream, new int[] {-4, -5}, null)) 
                        {
                            write(envelope.getRequest(), outStream);
                        }
                        break;
                    case "POSTGENERAL":
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-4, -5}, null))
                        {
                        	writeGeneral(envelope.getRequest(), outStream);
                        }
                        break;
                    case "READ":
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, "READ") &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                                cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                                checkExceptions(envelope.getRequest(), outStream, new int[] {-3, -10, -6}, "READ"))
                        {
                            read(envelope.getRequest(), outStream);
                        }
                        break;
                    case "READGENERAL":
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, "READGENERAL") &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-6, -10}, "READGENERAL"))
                        {
                            readGeneral(envelope.getRequest());
                        }
                        break;
                    case "READCOMPLETE":
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()) &&
                        		checkExceptions(envelope.getRequest(), outStream, new int[] {-3}, null))
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
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            wtsRequest(envelope.getRequest(), false,  outStream);
                        }
                    	break;
                    case "WTSGENERAL":
                        checkDelivered(envelope);
                        if(checkExceptions(envelope.getRequest(), outStream, new int[] {-7, -1}, null) &&
                        		cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs(userIdMap.get(envelope.getRequest().getPublicKey()))) &&
                        		cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            wtsRequest(envelope.getRequest(), true, outStream);
                        }
                        break;
                    case "ECHO":
                        if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs("server" + envelope.getRequest().getPort())) &&
                                   cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            checkEcho(envelope);
                        }
                        break;
                    case "READY":
                        if(cryptoManager.verifyRequest(envelope.getRequest(), envelope.getSignature(), cryptoManager.getPublicKeyFromKs("server" + envelope.getRequest().getPort())) &&
                                   cryptoManager.checkNonce(envelope.getRequest().getPublicKey(), envelope.getRequest().getServerNonce()))
                        {
                            checkReady(envelope);
                        }
                        break;

                    case "DELETEALL":
                        deleteUsers(outStream);
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
                    case "ATOMIC_WRITE_FLAG_TRUE":
                        atomicWriteFlag = true;
                        break;
                    case "ATOMIC_WRITE_FLAG_FALSE":
                        atomicWriteFlag = false;
                        break;
                    case "CONCURRENT_WRITE_FLAG_TRUE":
                        concurrentWrite = true;
                        break;
                    case "CONCURRENT_WRITE_FLAG_FALSE":
                        concurrentWrite = false;
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


    private void checkReady(Envelope envelope){
        System.out.println("CHECK READY PORT:" + serverPort);
        synchronized (readys){
            if(readys.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()) == null){
                readys.put(envelope.getRequest().getEnvelope().getRequest().getPublicKey(), new ConcurrentHashMap<>());
                readys.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).put(envelope.getRequest().getPublicKey(), envelope.getRequest().getEnvelope());
            }
            else{
                readys.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).put(envelope.getRequest().getPublicKey(), envelope.getRequest().getEnvelope());
            }
        }

        HashMap<String, Integer> counter = new HashMap<>();

        for(Envelope entry: readys.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).values()){
            if(counter.containsKey(entry.toString())){
                counter.put(entry.toString(), counter.get(entry.toString()) + 1);
                if(counter.get(entry.toString()) > 2 * nFaults && (delivered.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()) == null ||
                                                                       !delivered.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()))){
                    delivered.put(envelope.getRequest().getEnvelope().getRequest().getPublicKey(), true);
                    System.out.println("ativei a delivered, PORT: " + serverPort);
                    sentEcho.remove(entry.getRequest().getPublicKey());
                    sentReady.remove(entry.getRequest().getPublicKey());
                    echos.remove(entry.getRequest().getPublicKey());
                    readys.remove(entry.getRequest().getPublicKey());
                    break;
                }

                else if(counter.get(entry.toString()) > nFaults && (sentReady.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()) == null)){
                    System.out.println("ENTREI AQUI PORT: " + serverPort);
                    sentReady.put(envelope.getRequest().getEnvelope().getRequest().getPublicKey(), true);
                    broadcastReady(entry);

                }
            }
            else{
                counter.put(entry.toString(), 1);
            }

        }

    }

    private void checkEcho(Envelope envelope){
        System.out.println("CHECK ECHO PORT:" + serverPort);
        if(echos.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()) == null){
            System.out.println("1");
            echos.put(envelope.getRequest().getEnvelope().getRequest().getPublicKey(), new ConcurrentHashMap<>());
            echos.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).put(envelope.getRequest().getPublicKey(), envelope.getRequest().getEnvelope());
        }
        else{
            System.out.println("2");
            echos.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).put(envelope.getRequest().getPublicKey(), envelope.getRequest().getEnvelope());
        }

        HashMap<String, Integer> counter = new HashMap<>();

        for(Envelope entry: echos.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()).values()){
            System.out.println("3");
            if(counter.containsKey(entry.toString())){
                System.out.println("4");
                counter.put(entry.toString(), counter.get(entry.toString()) + 1);
                System.out.println("1" + (counter.get(entry.toString()) > nQuorum));
                System.out.println("2" + (sentReady.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey())));
                System.out.println("3" + userIdMap.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()));
                if(counter.get(entry.toString()) > nQuorum && (sentReady.get(envelope.getRequest().getEnvelope().getRequest().getPublicKey()) == null )){
                    System.out.println("5");
                    sentReady.put(envelope.getRequest().getEnvelope().getRequest().getPublicKey(), true);
                    System.out.println("PUS O SENTREADY DO: " + envelope.getRequest().getPublicKey());

                    broadcastReady(entry);
                }
            }
            else{
                counter.put(entry.toString(), 1);
            }

        }


    }

    private void broadcastReady(Envelope envelope){
        System.out.println("BROADCAST READY PORT: " + serverPort);
        Thread[] tasks = new Thread[nServers];

        int i = 0;
        while( i < nServers){
            if((PORT + i) == Integer.parseInt(serverPort)){
                Request request = new Request("READY", envelope, cryptoManager.getPublicKeyFromKs("server"), null, Integer.parseInt(serverPort));
                checkReady(new Envelope(request));
            }

            else{
                int finalI = i;
                tasks[i] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try(ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", PORT + finalI).getOutputStream())) {
                            byte[] nonce = startOneWayHandshakeServer(PORT + finalI);
                            Request request = new Request("READY", envelope, cryptoManager.getPublicKeyFromKs("server"), nonce, Integer.parseInt(serverPort));
                            sendRequest(request, outputStream);
                        } catch (NonceTimeoutException e) {
                            e.printStackTrace();
                        } catch (IntegrityException e) {
                            e.printStackTrace();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                tasks[i].start();
            }
            i++;
        }

    }

    private void checkDelivered(Envelope envelope){

        delivered.put(envelope.getRequest().getPublicKey(), false);

        System.out.println("ativei a delivered, PORT: " + serverPort);
        sentEcho.remove(envelope.getRequest().getPublicKey());
        sentReady.remove(envelope.getRequest().getPublicKey());
        echos.remove(envelope.getRequest().getPublicKey());
        readys.remove(envelope.getRequest().getPublicKey());

        if(sentEcho.get(envelope.getRequest().getPublicKey()) == null) {
            sentEcho.put(envelope.getRequest().getPublicKey(), true);

            Thread[] tasks = new Thread[nServers];

            int i = 0;
            while (i < nServers) {
                if ((PORT + i) == Integer.parseInt(serverPort)) {
                    Request request = new Request("ECHO", envelope, cryptoManager.getPublicKeyFromKs("server"), null, Integer.parseInt(serverPort));
                    checkEcho(new Envelope(request));
                } else {
                    int finalI = i;
                    tasks[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try (ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", PORT + finalI).getOutputStream())) {
                                byte[] nonce = startOneWayHandshakeServer(PORT + finalI);
                                Request request = new Request("ECHO", envelope, cryptoManager.getPublicKeyFromKs("server"), nonce, Integer.parseInt(serverPort));
                                sendRequest(request, outputStream);
                            } catch (NonceTimeoutException e) {
                                e.printStackTrace();
                            } catch (IntegrityException e) {
                                e.printStackTrace();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    tasks[i].start();
                }
                i++;
            }
            int timeout = 0;
            while (!delivered.get(envelope.getRequest().getPublicKey())) {
                try {
                    Thread.sleep(50);
                    timeout++;
                    System.out.println("tou a espera, PORT: " + serverPort);

                    if (timeout == 1000) {
                        break; //lançar exceçao
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            delivered.remove(envelope.getRequest().getPublicKey());
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
        userIdMap.put(request.getPublicKey(), username);
        saveUserIdMap();
        usersBoards.put(request.getPublicKey(), new Pair<>(0, new AnnouncementBoard(request.getUsername())));
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
    	System.out.println("SERVER ON PORT " + this.serverPort + ": WRITE METHOD");
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
            usersBoards.get(request.getPublicKey()).getSecond().addAnnouncement(announcementObject); //update val with the new post
            saveUsersBoards();

            incrementTotalAnnouncs();
            saveTotalAnnouncements();

        }
        if(listening.contains(request.getPublicKey())) { // no one is reading from who is writing
            for(Map.Entry<PublicKey, Pair<Integer, Integer>> entry : listening.get(request.getPublicKey()).entrySet()) {  //for every listening[q]
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

    public void addConcurrentPost(JSONObject object, byte[] signature) {
        System.out.println("CONCURRENT POST");
        ArrayList<Pair<JSONObject, byte[]>> new_announcements = new ArrayList<>();
        String user = (String) object.get("user");
        int i = 0;
        while(i < generalBoard.getSecond().getAnnoucements().size() - 1) {
            if((int) generalBoard.getSecond().getRawAnnouncements().get(i).getFirst().get("ts") == (int) object.get("ts")) {
                JSONObject older = (JSONObject) generalBoard.getSecond().getRawAnnouncements().get(i).getFirst().get("user");
                String older_user = (String) older.get("user");

                JSONObject next_older = (JSONObject) generalBoard.getSecond().getRawAnnouncements().get(i+1).getFirst().get("user");
                String next_user = (String) next_older.get("user");

                if(user.compareTo(older_user) < 0){
                    new_announcements.add(new Pair<>(object, signature));
                    new_announcements.add(generalBoard.getSecond().getRawAnnouncements().get(i));
                    break;
                }

                if(user.compareTo(older_user) > 0 && user.compareTo(next_user) < 0) {
                    new_announcements.add(generalBoard.getSecond().getRawAnnouncements().get(i));
                    new_announcements.add(new Pair<>(object, signature));
                    break;
                }
            }
            else{
                new_announcements.add(generalBoard.getSecond().getRawAnnouncements().get(i));
            }

        }
        while(i < generalBoard.getSecond().getAnnoucements().size()){
            new_announcements.add(generalBoard.getSecond().getRawAnnouncements().get(i));
        }

        generalBoard.getSecond().setAnnoucements(new_announcements);

    }
    
    @SuppressWarnings("unchecked")
	private void writeGeneral(Request request, ObjectOutputStream outStream) {
        System.out.println("SERVER ON PORT " + this.serverPort + ": WRITE GENERAL METHOD");
        if(request.getTs() >= generalBoard.getFirst()) {

            // Get userName from keystore
            if(concurrentWrite){
                try {
                    System.out.println("General gonna sleep");
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String username = userIdMap.get(request.getPublicKey());

            int[] refAnnouncements = request.getAnnouncements();
            // Write to file
            JSONObject announcementObject =  new JSONObject();
            announcementObject.put("id", Integer.toString(getTotalAnnouncements()));
            announcementObject.put("user", username);
            announcementObject.put("message", request.getMessage());
            announcementObject.put("ts", request.getTs());
            announcementObject.put("ref", refAnnouncements);

            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy 'at' HH:mm");
            announcementObject.put("date", ft.format(dNow).toString());


            if(refAnnouncements != null) {
                JSONArray annoucementsList = new JSONArray();
                for(int i = 0; i < refAnnouncements.length; i++){
                    annoucementsList.add(Integer.toString(refAnnouncements[i]));
                }
                announcementObject.put("ref_announcements", annoucementsList);
            }

            // ja n ta a escrever
            if(request.getTs() == generalBoard.getFirst()) {
                addConcurrentPost(announcementObject, request.getSignature());
            }
            else {
            	generalBoard.setFirst(request.getTs());
                generalBoard.getSecond().addAnnouncement(announcementObject, request.getSignature());
            }

            saveGeneralBoard();

            incrementTotalAnnouncs();
            saveTotalAnnouncements();

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
            listening.get(request.getPublicKeyToReadFrom()).put(request.getPublicKey(), new Pair<Integer,Integer>(request.getRid(), request.getNumber()));  //listening [p] := r ;
        }
        else {
            listening.put(request.getPublicKeyToReadFrom(), new ConcurrentHashMap<>()); //listening [p] := r ;
            listening.get(request.getPublicKeyToReadFrom()).put(request.getPublicKey(), new Pair<Integer,Integer>(request.getRid(), request.getNumber()));
        }

        System.out.println("SERVER ON PORT " + this.serverPort + ": READ METHOD");

        if(atomicWriteFlag){
            try{
                System.out.println("Gonna sleep");
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int total = request.getNumber();

        try(ObjectOutputStream outputStream = new ObjectOutputStream(new Socket("localhost", getClientPort(userIdMap.get(request.getPublicKey()))).getOutputStream())) {

            JSONObject announcementsToSend = new JSONObject();
            announcementsToSend.put("announcementList", usersBoards.get(request.getPublicKeyToReadFrom()).getSecond().getAnnouncements(total));

            // Send response to client
            // ------> Handshake one way
            byte[] nonce = startOneWayHandshake(userIdMap.get(request.getPublicKey()));

            sendRequest(new Request("VALUE", request.getRid(), usersBoards.get(request.getPublicKeyToReadFrom()).getFirst(), nonce, announcementsToSend, cryptoManager.getPublicKeyFromKs("server")), outputStream);
        } catch (SocketException e){
            return;  //if client socket is closed it means he already got enough answers or even byzantine client
        } catch(Exception e) {
            e.printStackTrace();
            sendResponse(new Response(false, -8, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), "READ"), outStream);
        }
        
    }
    

    //////////////////////////////////////////////////
    //				   READ GENERAL
    //////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
	private void readGeneral(Request request) {

        System.out.println("SERVER ON PORT " + this.serverPort + ": READ GENERAL method");

        int total;
        if(request.getNumber() == 0) { //all posts
            total = generalBoard.getSecond().size();
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
    }

    private void readComplete(Request request) {
    	if(listening.get(request.getPublicKeyToReadFrom()) != null &&
    			listening.get(request.getPublicKeyToReadFrom()).get(request.getPublicKey()) != null &&
    			(request.getRid() == listening.get(request.getPublicKeyToReadFrom()).get(request.getPublicKey()).getFirst())) {
            listening.get(request.getPublicKeyToReadFrom()).remove(request.getPublicKey());
        }
    }
    
    
    //////////////////////////////////////////////
    //				   SEND WTS
    //////////////////////////////////////////////
    
    private void wtsRequest(Request request, boolean isGeneral, ObjectOutputStream outStream) {
    	int wts = 0;
    	if(isGeneral) {
            wts = generalBoard.getFirst();
        }
    	else {
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
            if(replayFlag && !handshake) {
                outputStream.writeObject(oldEnvelope);
            }
            /*********************************************************************************************************************/
            else {
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

    
    private void newListener() {
        (new Thread(this)).start();
    }
    
    
////////////////////////////////////////////////////////////////////////////////
//
//  Method used to delete Tests' populate && Shut down Server && Start server
//
////////////////////////////////////////////////////////////////////////////////

    public void deleteUsers(ObjectOutputStream outputStream) throws IOException {

        System.out.println("SERVER ON PORT " + this.serverPort + ": DELETE OPERATION");

        userIdMap.clear();
        saveUserIdMap();

        String path = storagePath;

        FileUtils.deleteDirectory(new File(path));
        File files = new File(path);
        files.mkdirs();

        setTotalAnnouncements(0);
        saveTotalAnnouncements();
        
        outputStream.writeObject(new Envelope(new Request("DEL_ACK")));

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
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(announcementBoardsPathCopy))) {
            out.writeObject(usersBoards);
            System.out.println("SERVER ON PORT " + this.serverPort + ": Created updated copy of Announcement Boards");

            File original = new File(announcementBoardsPath);
            File copy = new File(announcementBoardsPathCopy);

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

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(announcementBoardsPath))) {
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
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(generalBoardPathCopy))) {
            out.writeObject(generalBoard);
            System.out.println("SERVER ON PORT " + this.serverPort + ": Created updated copy of the General Board");

            File original = new File(generalBoardPath);
            File copy = new File(generalBoardPathCopy);

            if(original.delete()) {
                copy.renameTo(original);
            }

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void getGeneralBoard() {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(generalBoardPath))) {
            generalBoard = (Pair<Integer, GeneralBoard>) in.readObject();
        } catch (ClassNotFoundException c) {
            System.out.println("SERVER ON PORT " + this.serverPort + ": Pair<Integer, GeneralBoard> class not found");
            c.printStackTrace();
            return;
        } catch(FileNotFoundException e) {
            generalBoard = new Pair<Integer, GeneralBoard>(0, new GeneralBoard());
            createOriginalGeneralBoard();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createOriginalGeneralBoard() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(generalBoardPath))) {
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


    private void incrementTotalAnnouncs() {
        totalAnnouncements.incrementAndGet();
    }

    private void setTotalAnnouncements(int value) {
        totalAnnouncements.set(value);
    }

    private int getTotalAnnouncements() {
        return totalAnnouncements.get();
    }

    private void saveTotalAnnouncements() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(totalAnnouncementsPathCopy))) {
            out.writeObject(totalAnnouncements.get());
            System.out.println("SERVER ON PORT " + this.serverPort + ": Serialized data of Total Announcements saved in copy");

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
    public boolean checkExceptions(Request request, ObjectOutputStream outStream, int[] codes, String operationType) {
        for (int i = 0; i < codes.length; i++) {
            switch(codes[i]) {
                // ## UserNotRegistered ## -> check if user is registered
                case -1:
                    if(!userIdMap.containsKey(request.getPublicKey())) {
                        sendResponse(new Response(false, -1, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), operationType), outStream);
                        return false;
                    }
                    break;
                // ## AlreadyRegistered ## -> check if user is already registered
                case -2:
                    if (userIdMap.containsKey(request.getPublicKey())) {
                        sendResponse(new Response(false, -2, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), operationType), outStream);
                        return false;
                    }
                    break;
                // ## UserNotRegistered ## -> [READ] check if user TO READ FROM is registered
                case -3:
                    if(!userIdMap.containsKey(request.getPublicKeyToReadFrom())) {
                        sendExceptionCode(request.getPublicKey(), request.getClientNonce(), -3, operationType);
                        return false;
                    }
                    break;
                // ## MessageTooBig ## -> Check if message length exceeds 255 characters
                case -4:
                    if(request.getMessage().length() > 255) {
                        sendResponse(new Response(false, -4, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), operationType), outStream);
                        return false;
                    }
                    break;
                // ## InvalidAnnouncement ## -> checks if announcements referred by the user are valid
                case -5:
                    if(request.getAnnouncements() != null && !checkValidAnnouncements(request.getAnnouncements())) {
                        sendResponse(new Response(false, -5, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), operationType), outStream);
                        return false;      
                    }
                    break;
                // ## InvalidPostsNumber ## -> check if number of requested posts are bigger than zero
                case -6:
                    if (request.getNumber() < 0) {
                        sendExceptionCode(request.getPublicKey(), request.getClientNonce(), -6, operationType);
                        return false;
                    }
                    break;
                // ## UnknownPublicKey ## -> Check if key is null or unknown by the Server. If method is read, check if key to read from is null
                case -7:
                    if (request.getPublicKey() == null || cryptoManager.checkKey(request.getPublicKey()) == "" || (request.getOperation().equals("READ") && (request.getPublicKeyToReadFrom() == null || cryptoManager.checkKey(request.getPublicKeyToReadFrom()) == ""))) {
                    	sendResponse(new Response(false, -7, request.getClientNonce(), cryptoManager.getPublicKeyFromKs("server"), operationType), outStream);
                        return false;
                    }
                    break;
                // ## TooMuchAnnouncements ## -> Check if user is trying to read more announcements that Board number of announcements
                case -10:
                    if ((request.getOperation().equals("READ") && request.getNumber() > usersBoards.get(request.getPublicKeyToReadFrom()).getSecond().size()) || (request.getOperation().equals("READGENERAL") && request.getNumber() > generalBoard.getSecond().size())) {
                        sendExceptionCode(request.getPublicKey(), request.getClientNonce(), -10, operationType);
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void sendExceptionCode(PublicKey clientKey, byte[] clientNonce, int code, String operationType) {
        int clientPort = getClientPort(userIdMap.get(clientKey));
        int ts = usersBoards.get(clientKey).getFirst();
        try(ObjectOutputStream newOutputStream = new ObjectOutputStream(new Socket("localhost", clientPort).getOutputStream())) {
            sendResponse(new Response(false, code, clientNonce, cryptoManager.getPublicKeyFromKs("server"), ts, operationType), newOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
//////////////////////////////////////////
//
//			Auxiliary methods
//
//////////////////////////////////////////
    
    private int getClientPort(String client) {
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

    private byte[] startOneWayHandshakeServer(int port) throws NonceTimeoutException, IntegrityException {
        Envelope nonceEnvelope = askForServerNonce(cryptoManager.getPublicKeyFromKs("server"), port);
        if(cryptoManager.verifyResponse(nonceEnvelope.getResponse(), nonceEnvelope.getSignature(), "server" + port)) {
            return nonceEnvelope.getResponse().getNonce();
        } else {
            throw new IntegrityException("Integrity Exception");
        }
    }

    private Envelope askForServerNonce(PublicKey serverKey, int port) throws NonceTimeoutException {
        try(Socket socket = new Socket("localhost", port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            return sendReceive(new Request("NONCE", serverKey), outputStream, inputStream);
        } catch (IOException e) {
            throw new NonceTimeoutException("The operation was not possible, please try again!"); //IOException apanha tudo
        }
    }
    
}