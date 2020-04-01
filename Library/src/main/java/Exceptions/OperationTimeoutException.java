package Exceptions;

public class OperationTimeoutException extends Exception{

    public OperationTimeoutException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}
