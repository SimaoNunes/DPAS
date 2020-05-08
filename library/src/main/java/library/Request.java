package library;

import org.json.simple.JSONObject;

import java.io.Serializable;
import java.security.PublicKey;

public class Request implements Serializable {
	
    private String operation;
    private byte[] serverNonce = null;   //server generated the nonce
    private byte[] clientNonce = null;   //client generated the nonce
    private PublicKey publicKey = null;
    private PublicKey publicKeyToReadFrom = null;
    private String message = null;
    private int number = -1;
    private int[] announcements = null;
    private int ts;
    private int rid;
    private int port;
    private JSONObject jsonObject = null;
    
    ////////////////////////////////////////////////////////////////
    //				                                         	  //
    //   Different constructors that allow different operations   //
    //	  	     		                                          //
    ////////////////////////////////////////////////////////////////

    public Request(String operation){
        this.operation = operation;
    }

    public Request(String operation, PublicKey key, String message, int[] announcs, byte[] serverNonce, byte[] clientNonce, int ts) {
        this.operation = operation;
        this.publicKey = key;
        this.message = message;
        this.announcements = announcs;
        this.serverNonce = serverNonce;
        this.clientNonce = clientNonce;
        this.ts = ts;
    }

    // Register
    public Request(String operation, PublicKey key, byte[] serverNonce, byte[] clientNonce){
    	this.operation = operation;
        this.publicKey = key;
        this.serverNonce = serverNonce;
        this.clientNonce = clientNonce;
    }
    
    // Register (DELETEALL) (ASK FOR NONCE)
    public Request(String operation, PublicKey key){
    	this.operation = operation;
        this.publicKey = key;
    }

    
    // When the CLIENT requests for a read operation - now we don't need the client's nounce
    public Request(String operation, PublicKey key, PublicKey publicKeyToReadFrom, int number, byte[] serverNonce, int rid) {
    	this.operation = operation;
        this.publicKey = key;
        this.publicKeyToReadFrom = publicKeyToReadFrom;
        this.number = number;
        this.serverNonce = serverNonce;
        this.rid = rid;
    }

    // When the CLIENT sends a read complete operation
    public Request(String operation, PublicKey key, PublicKey publicKeyToReadFrom, byte[] serverNonce, int rid) {
    	this.operation = operation;
        this.publicKey = key;
        this.publicKeyToReadFrom = publicKeyToReadFrom;
        this.serverNonce = serverNonce;
        this.rid = rid;
    }

    // ReadGeneral
    public Request(String operation, PublicKey key, int number, byte[] serverNonce, byte[] clientNonce) {
    	this.operation = operation;
        this.publicKey = key;
        this.number = number;
        this.serverNonce = serverNonce;
        this.clientNonce = clientNonce;
    }

    //VALUE
    public Request(String operation, int rid, int ts, byte[] nonce, JSONObject object, int port){
        this.operation = operation;
        this.jsonObject = object;
        this.clientNonce = nonce;
        this.rid = rid;
        this.ts = ts;
        this.port = port;
    }


    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
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

    public byte[] getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(byte[] nonceServer) {
        this.serverNonce = nonceServer;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

	public PublicKey getPublicKeyToReadFrom() {
		return publicKeyToReadFrom;
	}

	public void setPublicKeyToReadFrom(PublicKey publicKeyToReadFrom) {
		this.publicKeyToReadFrom = publicKeyToReadFrom;
	}
}
