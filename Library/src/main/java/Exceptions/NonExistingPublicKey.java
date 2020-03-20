package Exceptions;

public class NonExistingPublicKey extends Exception {

    public NonExistingPublicKey(String message){
        super(message);
    }
}
