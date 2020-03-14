package Library;

import java.io.Serializable;
import java.security.PublicKey;

public class Request implements Serializable {
    private String operation;
    private PublicKey publicKey = null;
    private String message = null;
    private int number = -1;
    private int[] announcements = null;

    /******
     * Fields used for the register operation.
     ******/
    private String name = null;
    private int id = -1;


    /****
    * Each constructor corresponds to each operation of the API.
     ****/
    public Request(String operation, PublicKey key){   //register
        this.publicKey = key;
        this.operation = operation;

    }

    public Request(String operation, PublicKey key, String message, int[] announcs){ //post
        this.publicKey = key;
        this.message = message;
        this.announcements = announcs;
        this.operation = operation;
    }

    public Request(String operation, PublicKey key, int number){ //read
        this.publicKey = key;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
