package Exceptions;

public class NonceTimeoutException extends Exception {

    public NonceTimeoutException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}
