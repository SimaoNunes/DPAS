package library;

import java.io.Serializable;
import java.security.PublicKey;

public class Request implements Serializable {
	
    private String operation;
    private byte[] nonceServer = null;   //server generated the nonce
    private byte[] nonceClient = null;   //client generated the nonce
    private PublicKey publicKey = null;
    private PublicKey publicKeyToReadFrom = null;
    private String message = null;
    private int number = -1;
    private int[] announcements = null;
    
    ////////////////////////////////////////////////////////////////
    //				                                         	  //
    //   Different constructors that allow different operations   //
    //	  	     		                                          //
    ////////////////////////////////////////////////////////////////

    public Request(String operation){
        this.operation = operation;
    }

    public Request(String operation, PublicKey key, String message,  int[] announcs, byte[] nonceServer, byte[] nonceClient){
        this.operation = operation;
        this.publicKey = key;
        this.message = message;
        this.announcements = announcs;
        this.nonceServer = nonceServer;
        this.nonceClient = nonceClient;
    }

    // Register
    public Request(String operation, PublicKey key, byte[] nonceServer, byte[] nonceClient){
    	this.operation = operation;
        this.publicKey = key;
        this.nonceServer = nonceServer;
        this.nonceClient = nonceClient;
    }
    
    // Register (DELETEALL) (ASK FOR NONCE)
    public Request(String operation, PublicKey key){
    	this.operation = operation;
        this.publicKey = key;
    }
    
    // Post
    public Request(String operation, PublicKey key, String message, int[] announcs) {
    	this.operation = operation;
    	this.publicKey = key;
        this.message = message;
        this.announcements = announcs;
    }
    
    // Read
    public Request(String operation, PublicKey key, PublicKey publicKeyToReadFrom, int number, byte[] nonceServer, byte[] nonceClient) {
    	this.operation = operation;
        this.publicKey = key;
        this.publicKeyToReadFrom = publicKeyToReadFrom;
        this.number = number;
        this.nonceServer = nonceServer;
        this.nonceClient = nonceClient;
    }
    
    // ReadGeneral
    public Request(String operation, PublicKey key, int number, byte[] nonceServer, byte[] nonceClient) {
    	this.operation = operation;
        this.publicKey = key;
        this.number = number;
        this.nonceServer = nonceServer;
        this.nonceClient = nonceClient;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int[] getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(int[] announcements) {
        this.announcements = announcements;
    }

    public byte[] getNonceServer() {
        return nonceServer;
    }

    public void setNonceServer(byte[] nonceServer) {
        this.nonceServer = nonceServer;
    }

    public byte[] getNonceClient() {
        return nonceClient;
    }

    public void setNonceClient(byte[] nonceClient) {
        this.nonceClient = nonceClient;
    }

	public PublicKey getPublicKeyToReadFrom() {
		return publicKeyToReadFrom;
	}

	public void setPublicKeyToReadFrom(PublicKey publicKeyToReadFrom) {
		this.publicKeyToReadFrom = publicKeyToReadFrom;
	}
}
