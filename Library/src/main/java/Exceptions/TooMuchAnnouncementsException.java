package Exceptions;

public class TooMuchAnnouncementsException extends Exception {
    public TooMuchAnnouncementsException(String message){
        super(message);
    }

    public String getMessage(){
        return this.getMessage();
    }
}
