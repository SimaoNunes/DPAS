package Exceptions;

public class InvalidPublicKeyException extends Exception {

    public InvalidPublicKeyException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}
