package Exceptions;

public class InvalidPostsNumberException extends Exception {

    public InvalidPostsNumberException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}
