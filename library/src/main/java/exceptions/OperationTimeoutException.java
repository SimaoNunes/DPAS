package exceptions;

public class OperationTimeoutException extends Exception{

    public OperationTimeoutException(String message){
        super(message);
    }

}
