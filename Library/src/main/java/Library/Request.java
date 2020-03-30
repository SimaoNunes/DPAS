package Library;

import java.io.Serializable;
import java.security.PublicKey;

public class Request implements Serializable {
	
    private String operation;
    private byte[] nonce = null;
    private PublicKey publicKey = null;
    private String message = null;
    private int number = -1;
    private int[] announcements = null;
    private String name = null;
    
    ////////////////////////////////////////////////////////////////
    //				                                         	  //
    //   Different constructors that allow different operations   //
    //	  	     		                                          //
    ////////////////////////////////////////////////////////////////

    public Request(String operation){
        this.operation = operation;
    }

    // Register or ??? Read ???
    public Request(String operation, PublicKey key, String name){
        this.publicKey = key;
        this.operation = operation;
        this.name = name;
    }
    
    // Register (DELETEALL)
    public Request(String operation, PublicKey key){
        this.publicKey = key;
        this.operation = operation;
    }
    
    // Post
    public Request(String operation, PublicKey key, String message, int[] announcs) {
        this.publicKey = key;
        this.message = message;
        this.announcements = announcs;
        this.operation = operation;
    }
    
    // Read
    public Request(String operation, PublicKey key, int number) {
        this.publicKey = key;
        this.number = number;
        this.operation = operation;
    }
    
    // Read
    public Request(String operation, int number) {
        this.number = number;
        this.operation = operation;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}
