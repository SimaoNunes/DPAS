package Exceptions;

public class UserNotRegisteredException extends Exception {

    public UserNotRegisteredException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}

