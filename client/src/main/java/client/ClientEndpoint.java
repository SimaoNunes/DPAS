package client;

import exceptions.*;
import library.Envelope;
import library.Request;
import library.Response;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientEndpoint {

    private Map<PublicKey, byte[]> serversNonces = null;
	private Map<PublicKey, byte[]> clientNonces  = null;
	private Map<PublicKey, Integer> serversPorts = null;
	
    private String serverAddress = null;

    private PrivateKey privateKey = null;
    private PublicKey publicKey  = null;
    private String userName = null;
    private CryptoManager cryptoManager = null;

    /********** Atomic Register Variables ************/
    int wts = -1; // -1 means we must ask server for the current wts
    int rid = 0;

    /************ Replication variables *************/
    private static final int PORT = 9000;
    private int nServers = 4;
    private int nFaults  = 1;
    private int nQuorum  = 2;
 

    private String registerErrorMessage = "There was a problem with your request, we cannot infer if you registered. Please try to login.";
    private String errorMessage = "There was a problem with your request. Please try again.";


    private ReplayAttacker replayAttacker = null;
    private boolean replayFlag = false;
    private boolean integrityFlag = false;

    /************************************************/

    public ClientEndpoint(String username) {
    	cryptoManager = new CryptoManager(username);
        setPrivateKey(cryptoManager.getPrivateKeyFromKs());
        setPublicKey(cryptoManager.getPublicKeyFromKs(username));
        setUsername(username);
        serversPorts = initiateServersPorts();

        clientNonces  = new HashMap<PublicKey, byte[]>();
    }

    public int getWts() {
        return wts;
    }

    public void setWts(int wts) {
        this.wts = wts;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getNFaults() {
        return nFaults;
    }

    public void setNFaults(int nFaults) {
        this.nFaults = nFaults;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    public void instantiateReplayAttacker() {
    	this.replayAttacker = new ReplayAttacker(this.serverAddress);
    }

    public boolean isReplayFlag() {
        return replayFlag;
    }

    public void setReplayFlag(boolean replayFlag) {
        this.replayFlag = replayFlag;
        if(replayFlag)
        	this.instantiateReplayAttacker();
    }

    public boolean isIntegrityFlag() {
		return integrityFlag;
	}

	public void setIntegrityFlag(boolean integrityFlag) {
		this.integrityFlag = integrityFlag;
	}

	public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public byte[] getServerNonce(PublicKey serverKey) {
        return serversNonces.get(serverKey);
    }

    public void setServerNonce(PublicKey serverKey, byte[] serverNonce) {
        this.serversNonces.put(serverKey, serverNonce);
    }

    public byte[] getClientNonce(PublicKey serverKey) {
        return clientNonces.get(serverKey);
    }

    public void setClientNonce(PublicKey serverKey, byte[] clientNonce) {
    	this.clientNonces.put(serverKey, clientNonce);
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
    

    private Socket createSocket(int port) throws IOException {
        return new Socket(getServerAddress(), port);
    }

    private ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    private ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    private Envelope sendReceive(Envelope envelope, int port) throws IOException, ClassNotFoundException {
        Socket socket = createSocket(port);
        socket.setSoTimeout(4000);
        // Sign envelope
        envelope.setSignature(cryptoManager.signRequest(envelope.getRequest(), getPrivateKey()));
        createOutputStream(socket).writeObject(envelope);
        Envelope responseEnvelope = (Envelope) createInputStream(socket).readObject();
        return responseEnvelope;
    }

    private void send(Envelope envelope, int port) throws IOException, ClassNotFoundException {
        Socket socket = createSocket(port);
        socket.setSoTimeout(4000);
        ObjectOutputStream out = createOutputStream(socket);
        // Sign envelope
        envelope.setSignature(cryptoManager.signRequest(envelope.getRequest(), getPrivateKey()));
        out.writeObject(envelope);
        out.close();
    }


//////////////////////////
//						//
//	Handshake Methods	//
//						//
//////////////////////////

    private Envelope askForServerNonce(PublicKey clientKey, int port) throws NonceTimeoutException {
        try {
        	return sendReceive(new Envelope(new Request("NONCE", clientKey)), port);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new NonceTimeoutException("The operation was not possible, please try again!"); //IOException apanha tudo
        }
        return null;
    }

    private void startHandshake(int port) throws NonceTimeoutException, IntegrityException {
    	Envelope nonceEnvelope = askForServerNonce(getPublicKey(), port);
    	if(cryptoManager.verifyResponse(nonceEnvelope.getResponse(), nonceEnvelope.getSignature())) {
    		setServerNonce(nonceEnvelope.getResponse().getServerKey(), nonceEnvelope.getResponse().getNonce());
            setClientNonce(nonceEnvelope.getResponse().getServerKey(), cryptoManager.generateClientNonce());
    	} else {
    		throw new IntegrityException("Integrity Exception");
    	}
    }

    private void startOneWayHandshake(int port) throws NonceTimeoutException, IntegrityException {
    	Envelope nonceEnvelope = askForServerNonce(getPublicKey(), port);
    	if(cryptoManager.verifyResponse(nonceEnvelope.getResponse(), nonceEnvelope.getSignature())) {
    		setServerNonce(nonceEnvelope.getResponse().getServerKey(), nonceEnvelope.getResponse().getNonce());
    	} else {
    		throw new IntegrityException("Integrity Exception");
    	}
    }

    private boolean checkNonce(Response response){
        if(Arrays.equals(response.getNonce(), getClientNonce(response.getServerKey()))) {
            setClientNonce(response.getServerKey(), null);
            setServerNonce(response.getServerKey(), null);
            return true;
        }
        setClientNonce(response.getServerKey(), null);
        setServerNonce(response.getServerKey(), null);
        return false;
    }    

    
///////////////////////
//					 //
//   API Functions   //
//	  	     		 //
///////////////////////
    
	//////////////////////////////////////////////////
	//				     REGISTER  					//
	//////////////////////////////////////////////////
    
    public int register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        // Port of first server
        int port = PORT;
        // Variables to store responses and their results
        int responses = 0;
        int[] results = new int[nServers];
        // Threads that will make the requests to the server
        CompletableFuture<Integer>[] tasks = new CompletableFuture[nServers];
        // Ask for wts to all Servers get results
        for (PublicKey key : serversPorts.keySet()) {

            int finalPort = port;

            tasks[serversPorts.get(key) - PORT] = CompletableFuture.supplyAsync(() -> {
				try {
					return registerMethod(key);
				} catch (AlreadyRegisteredException e) {
					return -2;
				} catch (UnknownPublicKeyException e) {
					return -7;
				} catch (FreshnessException e) {
					return -13;
            	} catch (NonceTimeoutException e) {
					return -11;
				} catch (IntegrityException e) {
					return -14;
				} catch (OperationTimeoutException e) {
					return -12;
				}
            });
            port++;
        }
        // Store results when they arrive
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].isDone()) {
                try {
                    results[responses++] = tasks[i].get().intValue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (responses > nQuorum) // OBA
                    break;
            }
            if (i == tasks.length - 1)
                i = 0;
        }
        // Get Quorum from the result to make a decision regarding the responses
        int result = getMajorityOfQuorumInt(results);
        switch (result) {
            case (-2):
                throw new AlreadyRegisteredException("This user is already registered!");
            case (-11):
                throw new NonceTimeoutException("Nonce timeout");
            case (-12):
                throw new OperationTimeoutException("Operation timeout");
            case (-13):
                throw new FreshnessException("Freshness Exception");
            case (-14):
                throw new IntegrityException("Integrity Exception");
            default:
            	return result;
        }
    }

    public int registerMethod(PublicKey key) throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

        startHandshake(serversPorts.get(key));

        Request request = new Request("REGISTER", getPublicKey(), getServerNonce(key), getClientNonce(key));

        Envelope envelopeRequest = new Envelope(request);
        
        /***** SIMULATE ATTACKER: changing the userX key to userY pubKey [in this case user3] *****/
        if(isIntegrityFlag()) {
        	envelopeRequest.getRequest().setPublicKey(cryptoManager.getPublicKeyFromKs("user3"));
        }
        /******************************************************************************************/
        try {
            Envelope envelopeResponse = sendReceive(envelopeRequest, serversPorts.get(key));
            /***** SIMULATE ATTACKER: send replayed messages to the server *****/
            if(isReplayFlag()){
                this.replayAttacker.sendReplays(envelopeRequest, 2);
            }
            /********************************************************************/
            if(!checkNonce(envelopeResponse.getResponse())) {
                throw new FreshnessException(registerErrorMessage);
            }
            if(!cryptoManager.verifyResponse(envelopeResponse.getResponse(), envelopeResponse.getSignature())) {
                throw new IntegrityException(registerErrorMessage);
            }
            ResponseChecker.checkRegister(envelopeResponse.getResponse());
            if(envelopeResponse.getResponse().getSuccess()){
                // On success, return 1
                return 1;
            }
            else{
                return 0;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch(IOException e){
            throw new OperationTimeoutException("There was a problem in the connection we cannot infer precisely if the register was successful. Please try to log in");
        }
    }

    
    //////////////////////////////////////////////////
    //					   POST  					//
    //////////////////////////////////////////////////
    
    public int post(String message, int[] announcs, boolean isGeneral) throws UserNotRegisteredException, MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        // Port of first server
        int port = PORT;
        // Ask Servers for actual wts in case we don't have it in memory
        if(wts == -1) {
        	askForWts();
        }
        // Variables to store responses and their results
        int responses = 0;
        int[] results = new int[nServers];
        // Threads that will make the requests to the server
        CompletableFuture<Integer>[] tasks = new CompletableFuture[nServers];
        // Make a post (write) to all Server and get results
        for (PublicKey key : serversPorts.keySet()) {

            tasks[serversPorts.get(key) - PORT] = CompletableFuture.supplyAsync(() -> {
                try {
                    return postMethod(message, announcs, isGeneral, key, wts);
                } catch (MessageTooBigException e) {
                    return -4;
                } catch (UserNotRegisteredException e) {
                    return -1;
                } catch (InvalidAnnouncementException e) {
                    return -5;
                } catch (NonceTimeoutException e) {
                    return -11;
                } catch (OperationTimeoutException e) {
                    return -12;
                } catch (FreshnessException e) {
                    return -13;
                } catch (IntegrityException e) {
                    return -14;
                }
            });
            port++;
        }
        // Store results when they arrive
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].isDone()) {
                try {
                    results[responses++] = tasks[i].get().intValue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (responses > nQuorum)
                    break;
            }
            if (i == tasks.length - 1)
                i = 0;
        }
        // Get Quorum from the result to make a decision regarding the responses
        int result = getQuorumInt(results);
        switch (result) {
            case (-1):
                throw new UserNotRegisteredException("User not Registered");
            case (-4):
                throw new MessageTooBigException("Message Too Big");
            case (-5):
                throw new InvalidAnnouncementException("Invalid announcement");
            case (-11):
                throw new NonceTimeoutException("Nonce timeout");
            case (-12):
                throw new OperationTimeoutException("Operation timeout");
            case (-13):
                throw new FreshnessException("Freshness Exception");
            case (-14):
                throw new IntegrityException("Integrity Exception");
        }
        return result;
    }
    
    public int postMethod(String message, int[] announcs, boolean isGeneral, PublicKey key, int ts) throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        startHandshake(serversPorts.get(key));
        return write(getPublicKey(), message, announcs, isGeneral, getServerNonce(key), getClientNonce(key), getPrivateKey(), serversPorts.get(key), ts);
    }

	public int write(PublicKey key, String message, int[] announcs, boolean isGeneral, byte[] serverNonce, byte[] clientNonce, PrivateKey privateKey, int port, int ts) throws InvalidAnnouncementException,
                                                                                                                                                                       UserNotRegisteredException, MessageTooBigException, OperationTimeoutException, FreshnessException, IntegrityException {
        Request request;
        
        if(isGeneral){
            request = new Request("POSTGENERAL", key, message, announcs, serverNonce, clientNonce, ts);
        }
        else{
            request = new Request("POST", key, message, announcs, serverNonce, clientNonce, ts);
        }

        Envelope envelopeRequest = new Envelope(request);

        /***** SIMULATE ATTACKER: changing the message (tamper) *****/
        if(isIntegrityFlag()) {
        	envelopeRequest.getRequest().setMessage("Olá, eu odeio-te");
        }
        /************************************************************/
        try {

            Envelope envelopeResponse = sendReceive(envelopeRequest, port);

            /***** SIMULATE ATTACKER: replay register *****/
            if(isReplayFlag()){
                this.replayAttacker.sendReplays(envelopeRequest, 2);
            }
            /**********************************************/
            if(!checkNonce(envelopeResponse.getResponse())){
                throw new FreshnessException(errorMessage);
            }
            if(!cryptoManager.verifyResponse(envelopeResponse.getResponse(), envelopeResponse.getSignature(), userName)){
                throw new IntegrityException(errorMessage);
            }
            ResponseChecker.checkPost(envelopeResponse.getResponse());
            // On success, return 1
            return 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new OperationTimeoutException("There was a problem in the connection, please do a read operation to confirm your post!");
        }
        return 0;
    }


    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////

    public JSONObject read(String announcUserName, int number) throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        rid += 1;

        //forall t > 0 do answers [t] := [⊥] N ;
        // No replicas on Server side [[[[[[[[[  TALVEZ NAO SEJA PRECISO  ]]]]]]]]]
        
        Listener listener = null;
        try {
            listener = new Listener(new ServerSocket(getClientPort()), nQuorum, getUsername());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (PublicKey key : serversPorts.keySet()) {

            CompletableFuture.runAsync(() -> readMethod(announcUserName, number, key, rid));

        }

        while(listener.getResult() == null){
            //wait for quorum
        }

        Response result = listener.getResult();
        System.out.println("RESULT: " + result.getSuccess() + result.getErrorCode());
        if(result.getSuccess()){
            return result.getJsonObject();
        }
        else{
            switch (result.getErrorCode()) {
                case (-1):
                    throw new UserNotRegisteredException("User not Registered");
                case (-3):
                    throw new UserNotRegisteredException("The user you're reading from is not registered!");  //OLD EXCEPTION FIX ME
                case (-6):
                    throw new InvalidPostsNumberException("Invalid announcements number to be read!");
                case (-10):
                    throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
                case (-11):
                    throw new NonceTimeoutException("Nonce timeout");
                case (-12):
                    throw new OperationTimeoutException("Operation timeout");
                case (-13):
                    throw new FreshnessException("Freshness Exception");
                case (-14):
                    throw new IntegrityException("Integrity Exception");

                default:
                    break;

            }
        }
        return new JSONObject();
    }

    public void readMethod(String announcUserName, int number, PublicKey key, int rid) {
        try {
            //  -----> Handshake one way
            startOneWayHandshake(serversPorts.get(key));

            //  -----> get public key to read from
            PublicKey pubKeyToReadFrom = cryptoManager.getPublicKeyFromKs(announcUserName);

            //  -----> send read operation to server

            Request request = new Request("READ", getPublicKey(), pubKeyToReadFrom, number, getServerNonce(key), rid);
            Envelope envelopeRequest = new Envelope(request, cryptoManager.signRequest(request, getPrivateKey()));
            send(envelopeRequest, serversPorts.get(key));


        } catch (ClassNotFoundException |
                NonceTimeoutException   |
                IOException             |
                IntegrityException   e) {
                e.printStackTrace();
                //Impossible to know if fault from the server when doing handshake or drop attack
                //throw new OperationTimeoutException("There was a problem in the connection, please do a read operation to confirm your post!");
        }
    }

	//////////////////////////////////////////////////
	//				   READ GENERAL					//
	//////////////////////////////////////////////////
    
    public JSONObject readGeneral(int number) throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        int responses = 0;
        int counter = 0;
        int port = PORT;

        Response[] results = new Response[(getNFaults() * 3 + 1) / 2 + 1];

        CompletableFuture<Response>[] tasks = new CompletableFuture[getNFaults() * 3 + 1];

        for (int i = 0; i < tasks.length; i++) {

            int finalPort = port;

            tasks[i] = CompletableFuture.supplyAsync(() -> readGeneralMethod(number, finalPort));
            port++;
        }
        while (responses < getNFaults()*3 + 1 / 2) {
            for (int i = 0; i < tasks.length; i++) {

                if (tasks[i].isDone()) {
                    System.out.println("is done");

                    responses++;

                    try {
                        results[counter++] = tasks[i].get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (responses > nQuorum)
                        break;
                }
                if (i == tasks.length - 1)
                    i = 0;
            }
        }
        Response result = getQuorumResponse(results);
        if(result.getSuccess()){
            return result.getJsonObject();
        }
        else{
            switch (result.getErrorCode()) {
                case (-1):
                    throw new UserNotRegisteredException("User not Registered");
                case (-6):
                    throw new InvalidPostsNumberException("Invalid announcements number to be read!");
                case (-10):
                    throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
                case (-11):
                    throw new NonceTimeoutException("Nonce timeout");
                case (-12):
                    throw new OperationTimeoutException("Operation timeout");
                case (-13):
                    throw new FreshnessException("Freshness Exception");
                case (-14):
                    throw new IntegrityException("Integrity Exception");

                default:
                    break;

            }
        }
        return new JSONObject();
    }

    public Response readGeneralMethod(int number, int port) {

        /*try {
            startHandshake(port);
        } catch (NonceTimeoutException e) {
            return new Response(false, -11, null);
        }

        Request request = new Request("READGENERAL", getPublicKey(), number, getServerNonce(publicKey), getClientNonce(port));
    	
    	Envelope envelopeRequest = new Envelope(request);

        try {
            Envelope envelopeResponse = sendReceive(envelopeRequest, port);
            /***** SIMULATE ATTACKER: send replayed messages to the server *****/
        /*
        if(isReplayFlag()){
            	this.replayAttacker.sendReplays(new Envelope(request, null), 2);
            }
            /*******************************************************************/
        /*
        if (!checkNonce(envelopeResponse.getResponse(), port)) {
                return new Response(false, -13, null);
                //throw new FreshnessException(errorMessage);
            }
            if(!cryptoManager.verifyResponse(envelopeResponse.getResponse(), envelopeResponse.getSignature(), userName)){
                return new Response(false, -14, null);
                //throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
			//checkReadGeneral(envelopeResponse.getResponse());
            return envelopeResponse.getResponse();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return new Response(false, -12, null);
        	//throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        }
        */return null;
    }
    
	//////////////////////////////////////////////////
	//				   ASK FOR WTS					//
	//////////////////////////////////////////////////
    
    private int askForWts() throws NonceTimeoutException, IntegrityException, OperationTimeoutException, FreshnessException, UserNotRegisteredException {
        // Port of first server
        int port = PORT;
        // No replicas on Server side [[[[[[[[[  TALVEZ NAO SEJA PRECISO  ]]]]]]]]]

        // Variables to store responses and their results
        int responses = 0;
        int[] results = new int[nServers];
        // Threads that will make the requests to the server
        CompletableFuture<Integer>[] tasks = new CompletableFuture[nServers];
        // Ask for wts to all Servers get results
        for (PublicKey key : serversPorts.keySet()) {

            tasks[serversPorts.get(key) - PORT] = CompletableFuture.supplyAsync(() -> {
				try {
					return askForSingleWts(key);
				} catch (UserNotRegisteredException e) {
					return -1;
            	} catch (FreshnessException e) {
					return -13;
            	} catch (NonceTimeoutException e) {
					return -11;
				} catch (IntegrityException e) {
					return -14;
				} catch (OperationTimeoutException e) {
					return -12;
				}
            });
            port++;
        }
        // Store results when they arrive
        for (int i = 0; i < tasks.length; i++) {
            if (tasks[i].isDone()) {
                try {
                    results[responses++] = tasks[i].get().intValue();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (responses > nQuorum) // OBA
                    break;
            }
            if (i == tasks.length - 1)
                i = 0;
        }
        // Get Quorum from the result to make a decision regarding the responses
        int result = getMajorityOfQuorumInt(results);
        switch (result) {
            case (-1):
                throw new UserNotRegisteredException("User not Registered");
            case (-11):
                throw new NonceTimeoutException("Nonce timeout");
            case (-12):
                throw new OperationTimeoutException("Operation timeout");
            case (-13):
                throw new FreshnessException("Freshness Exception");
            case (-14):
                throw new IntegrityException("Integrity Exception");
            default:
            	return result;
        }
	}

    
	private int askForSingleWts(PublicKey key) throws NonceTimeoutException, IntegrityException, OperationTimeoutException, FreshnessException, UserNotRegisteredException {
		// Make handshake with server
		startHandshake(serversPorts.get(key));
        // Make wts Request sign it and send inside envelope
        Request request = new Request("WTS", getPublicKey(), getServerNonce(key), getClientNonce(key));
    	Envelope envelopeRequest = new Envelope(request, cryptoManager.signRequest(request, getPrivateKey()));

    	// Get wts inside a Response
    	int singleWts = -666;
		try {
			Envelope envelopeResponse = sendReceive(envelopeRequest, serversPorts.get(key));
			// Verify Response's Freshness
            if(!checkNonce(envelopeResponse.getResponse())){
                throw new FreshnessException(errorMessage);
            }
	    	// Verify Response's Integrity
	        if(!cryptoManager.verifyResponse(envelopeResponse.getResponse(), envelopeResponse.getSignature())) {
	            throw new IntegrityException("EPA NAO SEI AINDA O QUE ESCREVER AQUI MAS UM ATACANTE ALTEROU A RESP DO WTS");
	        } else {
	        	singleWts = envelopeResponse.getRequest().getTs();
	        }
	        ResponseChecker.checkAskWts(envelopeResponse.getResponse());
	        return singleWts;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new OperationTimeoutException("Operation timeout");
		}
        return singleWts;
	}


//////////////////////////////////////////////////
//				 Auxiliary Methods				//
//////////////////////////////////////////////////

    private int getQuorumInt(int[] results) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < results.length; i++) {
            if (!map.containsKey(results[i])) {
                map.put(results[i], 1);
            } else {
                map.put(results[i], map.get(results[i]++));
                if (map.get(results[i]) > nQuorum) {
                    return results[i];
                }
            }
        }
        //NOT QUORUM
        return 0;
    }

    private Response getQuorumResponse(Response[] results) {
        System.out.println(results[0].getSuccess() + "\n" + results[0].getErrorCode());
        Response finalResult = results[0];
        for(int i = 1; i < results.length; i++) {
            System.out.println(results[i].getSuccess() + "\n" + results[i].getErrorCode());
            if (results[i].getSuccess() == finalResult.getSuccess() && results[i].getErrorCode() == finalResult.getErrorCode()) {
                continue;
            } else {
                System.out.println("Not quorum n sei o que fazer");
            }
        }
        return finalResult;
    }
    
    private int getMajorityOfQuorumInt(int[] results) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < results.length; i++) {
            if (!map.containsKey(results[i])) {
                map.put(results[i], 1);
            } else {
                map.put(results[i], map.get(results[i]) + 1);
                if (map.get(results[i]) > (results.length)/2) {
                    return results[i];
                }
            }
        }
        // REPLICAS HAVE MORE THAN F FAULTS
        return -666;
    }
    
    private int getClientPort(){
        try(BufferedReader reader = new BufferedReader(new FileReader("../clients_addresses.txt"))){
            String line;
            while((line = reader.readLine()) != null){
                String[] splitted = line.split(":");
                if(splitted[0].equals(userName)){
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

    private HashMap<PublicKey, Integer> initiateServersPorts(){
        return cryptoManager.initiateServersPorts(nServers);

    }
    

}
