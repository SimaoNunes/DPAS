package Client;

import Exceptions.*;
import Library.Envelope;
import Library.Request;
import Library.Response;
import org.json.simple.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

public class Client_Endpoint {

    private byte[] serverNonce = null;
    private byte[] clientNonce = null;

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;
    private String username = null;


    public Client_Endpoint(String username){
        setPrivateKey(getPrivateKeyFromKs(username));
        setPublicKey(getPublicKeyFromKs(username));
        setUsername(username);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    private Socket createSocket() throws IOException {
        return new Socket("localhost",9000);
    }

    private ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    private ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    private Envelope sendReceive(Envelope envelope) throws IOException, ClassNotFoundException {
        Socket socket = createSocket();
        createOutputStream(socket).writeObject(envelope);

        return (Envelope) createInputStream(socket).readObject();

    }

    //Crypto part

    public boolean checkHash(Envelope envelope){
        MessageDigest md;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;

        byte[] hash = decipher(envelope.getHash(), getServerPublicKey());

        try{
            md = MessageDigest.getInstance("SHA-256");

            out = new ObjectOutputStream(bos);
            out.writeObject(envelope.getResponse());
            out.flush();
            byte[] response_bytes = bos.toByteArray();

            return md.digest(response_bytes).equals(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PrivateKey getPrivateKeyFromKs(String username){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;
        PrivateKey key = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/" + username + "_keystore"), passphrase);
            key = (PrivateKey) ks.getKey(username, passphrase);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return key;

    }

    private PublicKey getPublicKeyFromKs(String username){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        PublicKey key = null;

        try{
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/" + username + "_keystore"), passphrase);
            return ks.getCertificate(username).getPublicKey();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }



    private byte[] askForServerNonce(PublicKey key){
        try {
             return sendReceive(new Envelope(new Request("NONCE", key))).getResponse().getNonce();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateClientNonce(){
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
    }

    private byte[] cipherRequest(Request request, PrivateKey key){

        MessageDigest md ;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;

        Cipher cipher;

        try{
            md = MessageDigest.getInstance("SHA-256");
            out = new ObjectOutputStream(bos);
            out.writeObject(request);
            out.flush();
            byte[] request_bytes = bos.toByteArray();
            byte[] request_hash = md.digest(request_bytes);

            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(request_hash);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decipher(byte[] bytes, PublicKey key){
        byte[] final_bytes = null;
        Cipher cipher;

        try{
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            final_bytes = cipher.doFinal(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return final_bytes;
    }

    public PublicKey getServerPublicKey(){
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = null;

        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);
            return ks.getCertificate("server").getPublicKey();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void startHandshake(PublicKey publicKey) {

        setServerNonce(askForServerNonce(publicKey));
        setClientNonce(generateClientNonce());

    }

    private boolean checkNonce(Response response){
        if(response.getNonce().equals(getClientNonce())){
            setClientNonce(null);
            setServerNonce(null);
            return true;
        }
        return false;
    }

 //////////////////////////////////////////////////////////////
 //															 //
 //   Methods that check if responses must throw exceptions  //
 //															 //
 //////////////////////////////////////////////////////////////
    
    public void checkRegister(Response response) throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -3){
                throw new InvalidPublicKeyException("Such key is invalid for registration, must have 2048 bits");
            }
            else if(error == -7){
                throw new UnknownPublicKeyException("Such key doesn't exist in the server side!");
            }
            else if(error == -2){
                throw new AlreadyRegisteredException("User with that public key already registered!");
            }
        }
    }

    public void checkPost(Response response) throws UserNotRegisteredException,
            InvalidPublicKeyException, MessageTooBigException, InvalidAnnouncementException {

        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1){
                throw new UserNotRegisteredException("User with this public key is not registered!");
            }
            else if(error == -3){
                throw new InvalidPublicKeyException("Invalid public key!");
            }
            else if(error == -4){
                throw new MessageTooBigException("Message cannot have more than 255 characters!");
            }
            else if(error == -5){
                throw new InvalidAnnouncementException("Announcements referenced do not exist!");
            }
        }

    }

    public void checkRead(Response response) throws UserNotRegisteredException,
            InvalidPublicKeyException, InvalidPostsNumberException, TooMuchAnnouncementsException {

        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1){
                throw new UserNotRegisteredException("User with this public key is not registered!");
            }
            else if(error == -3){
                throw new InvalidPublicKeyException("Invalid public key!");
            }
            else if(error == -6){
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10){
                throw new TooMuchAnnouncementsException("There are not that much announcements to be read!");
            }
        }
        
    }
    
    public void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException {

		if(!response.getSuccess()){
		    int error = response.getErrorCode();
		    if(error == -6){
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

    public int register() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {

        startHandshake(getPublicKey());

        Request request = new Request("REGISTER", getPublicKey(), getUsername(), getServerNonce(), getClientNonce());

        Envelope envelope_req = new Envelope(request, cipherRequest(request, getPrivateKey()));

        System.out.println(getClientNonce());
        System.out.println(getServerNonce());

        try {
            Envelope envelope_resp = sendReceive(envelope_req);

            if(!checkNonce(envelope_resp.getResponse()))          {
                //lançar exceçao, old message
            }

            if(!checkHash(envelope_resp)) {
                //lançar exceção
            }
            checkRegister(envelope_resp.getResponse());
            if(envelope_resp.getResponse().getSuccess()){
                // On success, return 1
                return 1;
            }
            else{
                return 0;
            }
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return 0
            e.printStackTrace();
            return 0;
        }
    }

    //////////////////////////////////////////////////
    //					   POST  					//
    //////////////////////////////////////////////////

    public int postAux(PublicKey key, String message, int[] announcs, boolean isGeneral, byte[] serverNonce, byte[] clientNonce, PrivateKey privateKey) throws InvalidAnnouncementException,
            UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException {
        Request request;
        if(isGeneral){
            request = new Request("POSTGENERAL", key, message, announcs, serverNonce, clientNonce);
        }
        else{
            request = new Request("POST", key, message, announcs, serverNonce, clientNonce);
        }

        Envelope envelope_req = new Envelope(request, cipherRequest(request, privateKey));

        try {
            Envelope envelope_resp = sendReceive(envelope_req);
            if(!checkNonce(envelope_resp.getResponse())){
                //lançar execeção, replay attack
            }

            if(!checkHash(envelope_resp)){
                //lançar exceção
            }
            checkPost(envelope_resp.getResponse());
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int post(String message, int[] announcs) throws MessageTooBigException, UserNotRegisteredException,
    		InvalidPublicKeyException, InvalidAnnouncementException {

        startHandshake(getPublicKey());

        return postAux(getPublicKey(), message, announcs, false, getServerNonce(), getClientNonce(), getPrivateKey());
    }
    
    public int postGeneral(String message, int[] announcs) throws  MessageTooBigException, UserNotRegisteredException,
            InvalidPublicKeyException, InvalidAnnouncementException {

        startHandshake(getPublicKey());

        return postAux(getPublicKey(), message, announcs, true, getServerNonce(), getClientNonce(), getPrivateKey());
    }

    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////

    public JSONObject read(PublicKey key, int number, PrivateKey privateKey) throws InvalidPostsNumberException, UserNotRegisteredException,
    		InvalidPublicKeyException, TooMuchAnnouncementsException {

        startHandshake(getPublicKey());

    	Request request = new Request("READ", key, number, getServerNonce(), getClientNonce());

        Envelope envelope_req = new Envelope(request, cipherRequest(request, getPrivateKey()));

        try {
            Envelope envelope_resp = sendReceive(envelope_req);

            if(!checkNonce(envelope_resp.getResponse())){
                //lançar execeção, replay attack
            }

            if(!checkHash(envelope_resp)){
                //lançar exceção
            }

			checkRead(envelope_resp.getResponse());
            return envelope_resp.getResponse().getJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject readGeneral(int number) throws InvalidPostsNumberException, TooMuchAnnouncementsException {

        Request request = new Request("READGENERAL", number);

        try {
            Envelope envelope = sendReceive(new Envelope(request, null));

            if(!checkHash(envelope)){
                //lançar exceção
            }
			checkReadGeneral(envelope.getResponse());
            return envelope.getResponse().getJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
