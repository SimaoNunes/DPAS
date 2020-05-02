package exceptions;

public class NonceTimeoutException extends Exception {

    public NonceTimeoutException(String message){
        super(message);
    }

}
