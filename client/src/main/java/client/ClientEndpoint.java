	package client;

import exceptions.*;
import library.Envelope;
import library.Request;
import library.Response;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;

public class ClientEndpoint {

    private byte[] serverNonce = null;
    private byte[] clientNonce = null;

    private String serverAddress = null;

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;
    private PublicKey serverPublicKey = null;
    private String userName = null;
    private CryptoManager criptoManager = null;

    private String registerErrorMessage = "There was a problem with your request, we cannot infer if you registered. Please try to login.";
    private String errorMessage = "There was a problem with your request. Please try again.";

    /*********** Simulated Attacks Flags ************/

    private boolean replay_flag = false;
    private boolean integrity_flag = false;

    /************************************************/

    public ClientEndpoint(String userName, String server){
    	criptoManager = new CryptoManager();
        setPrivateKey(criptoManager.getPrivateKeyFromKs(userName));
        setPublicKey(criptoManager.getPublicKeyFromKs(userName, userName));
        setServerPublicKey(criptoManager.getPublicKeyFromKs(userName, "server"));
        setUsername(userName);
        setServerAddress(server);
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String server_address) {
        this.serverAddress = server_address;
    }

    public boolean isReplayFlag() {
        return replay_flag;
    }

    public void setReplayFlag(boolean replay_flag) {
        this.replay_flag = replay_flag;
    }

    public boolean isIntegrityFlag() {
		return integrity_flag;
	}

	public void setIntegrityFlag(boolean integrity_flag) {
		this.integrity_flag = integrity_flag;
	}

	public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public PrivateKey getPrivateKey(){
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
    
    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(PublicKey serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    private Socket createSocket() throws IOException {
        return new Socket(getServerAddress(), 9000);
    }

    private ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    private ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    private Envelope sendReceive(Envelope envelope) throws IOException, ClassNotFoundException {
        Socket socket = createSocket();
        socket.setSoTimeout(4000);
        createOutputStream(socket).writeObject(envelope);
        return (Envelope) createInputStream(socket).readObject();
    }

    private void sendReplays(Envelope envelope, int n_replays){
        try {
            int i = 0;
            while(i < n_replays){
                Socket socket = createSocket();
                createOutputStream(socket).writeObject(envelope);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
//////////////////////////
//						//
//	Handshake Methods	//
//						//
//////////////////////////

    private byte[] askForServerNonce(PublicKey key) throws NonceTimeoutException {
        try {
             return sendReceive(new Envelope(new Request("NONCE", key))).getResponse().getNonce();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new NonceTimeoutException("The operation was not possible, please try again!"); //IOException apanha tudo
        }
        return new byte[0];
    }

    private void startHandshake(PublicKey publicKey) throws NonceTimeoutException {
        setServerNonce(askForServerNonce(publicKey));
        setClientNonce(criptoManager.generateClientNonce());
    }

    private boolean checkNonce(Response response){
        if(Arrays.equals(response.getNonce(), getClientNonce())) {
            setClientNonce(null);
            setServerNonce(null);
            return true;
        }
        setClientNonce(null);
        setServerNonce(null);
        return false;
    }

    
 //////////////////////////////////////////////////////////////
 //															 //
 //   Methods that check if Responses must throw exceptions  //
 //															 //
 //////////////////////////////////////////////////////////////
    
    public void checkRegister(Response response) throws AlreadyRegisteredException, UnknownPublicKeyException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -7) {
                throw new UnknownPublicKeyException("Such key doesn't exist in the server side!");
            }
            else if(error == -2) {
                throw new AlreadyRegisteredException("User with that public key already registered in the DPAS!");
            }
        }
    }

    public void checkPost(Response response) throws UserNotRegisteredException,
            MessageTooBigException, InvalidAnnouncementException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -4) {
                throw new MessageTooBigException("Message cannot exceed 255 characters!");
            }
            else if(error == -5) {
                throw new InvalidAnnouncementException("Announcements referenced do not exist!");
            }
        }
    }

    public void checkRead(Response response) throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -3) {
                throw new UserNotRegisteredException("The user you're reading from is not registered!");
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
            }
        }
    }
    
    public void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException, UserNotRegisteredException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1) {
                throw new UserNotRegisteredException("This user is not registered!");
            }
            else if(error == -6) {
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10) {
                throw new TooMuchAnnouncementsException("The number of announcements you've asked for exceeds the number of announcements existing in such board");
            }
		}
	}

    
 ///////////////////////
 //					  //
 //   API Functions   //
 //	  	     		  //
 ///////////////////////
    
	//////////////////////////////////////////////////
	//				     REGISTER  					//
	//////////////////////////////////////////////////

    public int register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

        startHandshake(getPublicKey());

        Request request = new Request("REGISTER", getPublicKey(), getServerNonce(), getClientNonce());

        Envelope envelopeRequest = new Envelope(request, criptoManager.cipherRequest(request, getPrivateKey()));
        
        /***** SIMULATE ATTACKER: changing the userX key to userY pubKey [in this case user3] *****/
        if(isIntegrityFlag()) {
        	envelopeRequest.getRequest().setPublicKey(criptoManager.getPublicKeyFromKs(userName, "user3"));
        }
        /******************************************************************************************/
        
        try {
            Envelope envelopeResponse = sendReceive(envelopeRequest);
            /***** SIMULATE ATTACKER: send replayed messages to the server *****/
            if(isReplayFlag()){
                sendReplays(envelopeRequest, 2);
            }
            /********************************************************************/
            if(!checkNonce(envelopeResponse.getResponse())) {
                throw new FreshnessException(registerErrorMessage);
            }
            if(!criptoManager.checkHash(envelopeResponse, userName)) {
                throw new IntegrityException(registerErrorMessage);
            }
            checkRegister(envelopeResponse.getResponse());
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

    public int postAux(PublicKey key, String message, int[] announcs, boolean isGeneral, byte[] serverNonce, byte[] clientNonce, PrivateKey privateKey) throws InvalidAnnouncementException,
                                                                                                                                                                       UserNotRegisteredException, MessageTooBigException, OperationTimeoutException, FreshnessException, IntegrityException {
        Request request;
        if(isGeneral){
            request = new Request("POSTGENERAL", key, message, announcs, serverNonce, clientNonce);
        }
        else{
            request = new Request("POST", key, message, announcs, serverNonce, clientNonce);
        }

        Envelope envelopeRequest = new Envelope(request, criptoManager.cipherRequest(request, privateKey));

        /***** SIMULATE ATTACKER: changing the message (tamper) *****/
        if(isIntegrityFlag()) {
        	envelopeRequest.getRequest().setMessage("Olá, eu odeio-te");
        }
        /************************************************************/

        try {

            Envelope envelopeResponse = sendReceive(envelopeRequest);
            /***** SIMULATE ATTACKER: replay register *****/
            if(isReplayFlag()){
                sendReplays(envelopeRequest, 2);
            }
            /**********************************************/
            if(!checkNonce(envelopeResponse.getResponse())){
                throw new FreshnessException(errorMessage);
            }
            if(!criptoManager.checkHash(envelopeResponse, userName)){
                throw new IntegrityException(errorMessage);
            }
            checkPost(envelopeResponse.getResponse());
            // On success, return 1
            return 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new OperationTimeoutException("There was a problem in the connection, please do a read operation to confirm your post!");
        }
        return 0;
    }

    public int post(String message, int[] announcs) throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        startHandshake(getPublicKey());
        return postAux(getPublicKey(), message, announcs, false, getServerNonce(), getClientNonce(), getPrivateKey());
    }
    
    public int postGeneral(String message, int[] announcs) throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        startHandshake(getPublicKey());
        return postAux(getPublicKey(), message, announcs, true, getServerNonce(), getClientNonce(), getPrivateKey());
    }

    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////

    public JSONObject read(String announcUserName, int number) throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

        startHandshake(getPublicKey());
        
        PublicKey pubKeyToReadFrom = criptoManager.getPublicKeyFromKs(userName, announcUserName);

    	Request request = new Request("READ", getPublicKey(), pubKeyToReadFrom, number, getServerNonce(), getClientNonce());

        Envelope envelopeRequest = new Envelope(request, criptoManager.cipherRequest(request, getPrivateKey()));
        
        /***** SIMULATE ATTACKER: changing the user to read from. User might think is going to read from user X but reads from Y [in this case user3] (tamper) *****/
        if(isIntegrityFlag()) {
        	envelopeRequest.getRequest().setPublicKeyToReadFrom(criptoManager.getPublicKeyFromKs(userName, "user3"));
        }
        /**********************************************************************************************************************************************************/

        try {
            Envelope envelopeResponse = sendReceive(envelopeRequest);
            /***** SIMULATE ATTACKER: send replayed messages to the server *****/
            if(isReplayFlag()){
                sendReplays(envelopeRequest, 2);
            }
            /*******************************************************************/
            if (!checkNonce(envelopeResponse.getResponse())) {
                throw new FreshnessException(errorMessage);
            }
            if (!criptoManager.checkHash(envelopeResponse, userName)) {
                throw new IntegrityException(errorMessage);
            }
            checkRead(envelopeResponse.getResponse());
            return envelopeResponse.getResponse().getJsonObject();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        } 
        return null;
    }

    public JSONObject readGeneral(int number) throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {

    	startHandshake(getPublicKey());

    	Request request = new Request("READGENERAL", getPublicKey(), number, getServerNonce(), getClientNonce());
    	
    	Envelope envelopeRequest = new Envelope(request, criptoManager.cipherRequest(request, getPrivateKey()));

        try {
            Envelope envelopeResponse = sendReceive(envelopeRequest);
            /***** SIMULATE ATTACKER: send replayed messages to the server *****/
            if(isReplayFlag()){
                sendReplays(new Envelope(request, null), 2);
            }
            /*******************************************************************/
            if (!checkNonce(envelopeResponse.getResponse())) {
                throw new FreshnessException(errorMessage);
            }
            if(!criptoManager.checkHash(envelopeResponse, userName)){
                throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
			checkReadGeneral(envelopeResponse.getResponse());
            return envelopeResponse.getResponse().getJsonObject();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        }
        return null;
    }    
}
