package Exceptions;

public class UnknownPublicKeyException extends Exception{

	public UnknownPublicKeyException(String message){
        super(message);
    }

        public String getMessage(){
            return this.getMessage();
        }

}
