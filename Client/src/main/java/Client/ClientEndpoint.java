	package Client;

import Exceptions.*;
import Library.Envelope;
import Library.Request;
import Library.Response;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.util.Arrays;

public class ClientEndpoint {

    private byte[] serverNonce = null;
    private byte[] clientNonce = null;

    private String server_address = null;

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;
    private PublicKey serverPublicKey = null;
    private String userName = null;
    private CryptoManager criptoManager = null;

    /***********Attack Tests Flags************/

    private boolean test_flag = false;
    private boolean nonce_flag = false;
    private boolean operation_flag = false;
    private boolean later_timeout = false;
    private boolean replay_flag = false;
    private boolean integrity_flag = false;

    /*****************************************/

    public ClientEndpoint(String userName, String server){
    	criptoManager = new CryptoManager();
        setPrivateKey(criptoManager.getPrivateKeyFromKs(userName));
        setPublicKey(criptoManager.getPublicKeyFromKs(userName, userName));
        setServerPublicKey(criptoManager.getPublicKeyFromKs(userName, "server"));
        setUsername(userName);
        setServer_address(server);
    }

    public String getServer_address() {
        return server_address;
    }

    public void setServer_address(String server_address) {
        this.server_address = server_address;
    }

    public boolean isReplay_flag() {
        return replay_flag;
    }

    public void setReplay_flag(boolean replay_flag) {
        this.replay_flag = replay_flag;
    }

    public boolean isNonce_flag() {
        return nonce_flag;
    }

    public void setNonce_flag(boolean nonce_flag) {
        this.nonce_flag = nonce_flag;
    }

    public boolean isOperation_flag() {
        return operation_flag;
    }

    public void setOperation_flag(boolean operation_flag) {
        this.operation_flag = operation_flag;
    }

    public boolean isLater_timeout() {
        return later_timeout;
    }

    public void setLater_timeout(boolean later_timeout) {
        this.later_timeout = later_timeout;
    }

    public boolean isTest_flag() {
        return test_flag;
    }

    public void setTest_flag(boolean test_flag) {
        this.test_flag = test_flag;
    }

    public boolean isIntegrity_flag() {
		return integrity_flag;
	}

	public void setIntegrity_flag(boolean integrity_flag) {
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
        return new Socket(getServer_address(), 9000);
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
        if(isTest_flag()){
            if(isNonce_flag() || isLater_timeout()){
                socket.setSoTimeout(20);
            }
        }
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
        } catch (SocketTimeoutException e) {
            throw new NonceTimeoutException("The operation was not possible, please try again!"); //IOException apanha tudo
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
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
    
    public void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException {
        if(!response.getSuccess()){
		    int error = response.getErrorCode();
		    if(error == -6) {
		        throw new InvalidPostsNumberException("Invalid announcements number to be read!");
		    }
		    else if(error == -10){
		        throw new TooMuchAnnouncementsException("There are not that much announcements to be read!");
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

        Envelope envelope_req = new Envelope(request, criptoManager.cipherRequest(request, getPrivateKey()));


        if(operation_flag){
            later_timeout = true;
        }
        
        // SIMULATE ATTACKER: changing the userX key to userY pubKey [in this case user3]
        if(isIntegrity_flag()) {
        	envelope_req.getRequest().setPublicKey(criptoManager.getPublicKeyFromKs(userName, "user3"));
        }

        try {
            Envelope envelope_resp = sendReceive(envelope_req);
            // SIMULATE ATTACKER: replay register
            if(replay_flag){
                sendReplays(envelope_req, 2);
            }
            if(!checkNonce(envelope_resp.getResponse())) {
                throw new FreshnessException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            if(!criptoManager.checkHash(envelope_resp, userName)) {
                throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            checkRegister(envelope_resp.getResponse());
            if(envelope_resp.getResponse().getSuccess()){
                // On success, return 1
                return 1;
            }
            else{
                return 0;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch(SocketTimeoutException e){
            throw new OperationTimeoutException("There was a problem in the connection we cannot infer precisely if the register was successful. Please try to log in");
        } catch (IOException e) {
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

        Envelope envelope_req = new Envelope(request, criptoManager.cipherRequest(request, privateKey));

        if(operation_flag){
            later_timeout = true;
        }
        // SIMULATE ATTACKER: changing the message (tamper)
        if(isIntegrity_flag()) {
        	envelope_req.getRequest().setMessage("Olá, eu odeio-te");
        }

        try {
            Envelope envelope_resp = sendReceive(envelope_req);
            // SIMULATE ATTACKER: replay register
            if(replay_flag){
                sendReplays(envelope_req, 2);
            }
            later_timeout = false;
            if(!checkNonce(envelope_resp.getResponse())){
                throw new FreshnessException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            if(!criptoManager.checkHash(envelope_resp, userName)){
                throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            checkPost(envelope_resp.getResponse());
            return 1;
        } catch (SocketTimeoutException e) {
            throw new OperationTimeoutException("There was a problem in the connection, please do a read operation to confirm your post!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e){
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

        Envelope envelope_req = new Envelope(request, criptoManager.cipherRequest(request, getPrivateKey()));

        if(operation_flag){
            later_timeout = true;
        }
        
        // SIMULATE ATTACKER: changing the read to use from. User might think is going to read from user X but reads from X [in this case user3] (tamper)
        if(isIntegrity_flag()) {
        	envelope_req.getRequest().setPublicKeyToReadFrom(criptoManager.getPublicKeyFromKs(userName, "user3"));
        }

        try {
            Envelope envelope_resp = sendReceive(envelope_req);
            // SIMULATE ATTACKER: replay post
            if(replay_flag){
                sendReplays(envelope_req, 2);
            }
            if (!checkNonce(envelope_resp.getResponse())) {
                throw new FreshnessException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            if (!criptoManager.checkHash(envelope_resp, userName)) {
                throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
            checkRead(envelope_resp.getResponse());
            return envelope_resp.getResponse().getJsonObject();
        } catch (SocketTimeoutException e){
            throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        } catch (IOException e) {
        	throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject readGeneral(int number) throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException {

        Request request = new Request("READGENERAL", number);

        if(operation_flag){
            later_timeout = true;
        }

        try {
            Envelope envelope = sendReceive(new Envelope(request, null));
            // SIMULATE ATTACKER: replay read
            if(replay_flag){
                sendReplays(new Envelope(request, null), 2);
            }

            if(!criptoManager.checkHash(envelope, userName)){
                throw new IntegrityException("There was a problem with your request, we cannot infer if you registered. Please try to login");
            }
			checkReadGeneral(envelope.getResponse());
            return envelope.getResponse().getJsonObject();

        } catch (SocketTimeoutException e) {
            throw new OperationTimeoutException("There was a problem with the connection, please try again!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    
}
