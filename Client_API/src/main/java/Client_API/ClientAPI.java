package Client_API;

import Exceptions.*;
import Library.Request;
import Library.Response;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class ClientAPI {


    public ClientAPI(){

    }

    private Socket createSocket() throws IOException {
        return new Socket("localhost",9000);
    }

    private ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    private ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    private Response sendReceive(Request request) throws IOException, ClassNotFoundException {
        Socket socket = createSocket();
        createOutputStream(socket).writeObject(request);

        return (Response) createInputStream(socket).readObject();

    }

    private byte[] generateNonce(){
        try {
             return sendReceive(new Request("NONCE")).getNonce();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
 //////////////////////////////////////////////////////////////
 //															 //
 //   Methods that check if responses must throw exceptions  //
 //															 //
 //////////////////////////////////////////////////////////////
    
    public void checkRegister(Response response) throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -3){
                throw new InvalidPublicKeyException("Such key is invalid for registration, must have 2048 bits");
            }
            else if(error == -7){
                throw new UnknownPublicKeyException("Such key doesn't exist in the server side!");
            }
            else if(error == -2){
                throw new AlreadyRegisteredException("User with that public key already registered!");
            }
        }
    }

    public void checkPost(Response response) throws UserNotRegisteredException,
            InvalidPublicKeyException, MessageTooBigException, InvalidAnnouncementException {

        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1){
                throw new UserNotRegisteredException("User with this public key is not registered!");
            }
            else if(error == -3){
                throw new InvalidPublicKeyException("Invalid public key!");
            }
            else if(error == -4){
                throw new MessageTooBigException("Message cannot have more than 255 characters!");
            }
            else if(error == -5){
                throw new InvalidAnnouncementException("Announcements referenced do not exist!");
            }
        }

    }

    public void checkRead(Response response) throws UserNotRegisteredException,
            InvalidPublicKeyException, InvalidPostsNumberException, TooMuchAnnouncementsException {

        if(!response.getSuccess()){
            int error = response.getErrorCode();
            if(error == -1){
                throw new UserNotRegisteredException("User with this public key is not registered!");
            }
            else if(error == -3){
                throw new InvalidPublicKeyException("Invalid public key!");
            }
            else if(error == -6){
                throw new InvalidPostsNumberException("Invalid announcements number to be read!");
            }
            else if(error == -10){
                throw new TooMuchAnnouncementsException("There are not that much announcements to be read!");
            }
        }
        
    }
    
    public void checkReadGeneral(Response response) throws InvalidPostsNumberException, TooMuchAnnouncementsException {

		if(!response.getSuccess()){
		    int error = response.getErrorCode();
		    if(error == -6){
		        throw new InvalidPostsNumberException("Invalid announcements number to be read!");
		    }
		    else if(error == -10){
		        throw new TooMuchAnnouncementsException("There are not that much announcements to be read!");
		    }
		}
		
	}

    
 ///////////////////////
 //					  //
 //   API Functions   //
 //	  	     		  //
 ///////////////////////
    
	//////////////////////////////////////////////////
	//				     REGISTER  					//
	//////////////////////////////////////////////////

    public int register(PublicKey key, String name) throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {

        Request request = new Request("REGISTER", key, name);
        try {
            Response response = sendReceive(request);
            // Check if response is bad (throws exception in case it is bad)
            checkRegister(response);

            if(response.getSuccess()){
                // On success, return 1
                return 1;
            }
            else{
                return 0;
            }
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return 0
            e.printStackTrace();
            return 0;
        }
    }

    //////////////////////////////////////////////////
    //					   POST  					//
    //////////////////////////////////////////////////

    public int postAux(PublicKey key, String message, int[] announcs, boolean isGeneral) throws InvalidAnnouncementException,
            UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException {
        Request request;
        if(isGeneral){
            request = new Request("POSTGENERAL", key, message, announcs);
        }
        else{
            request = new Request("POST", key, message, announcs);
        }

        try {
            Response response = sendReceive(request);
            checkPost(response);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int post(PublicKey key, String message, int[] announcs) throws MessageTooBigException, UserNotRegisteredException,
    		InvalidPublicKeyException, InvalidAnnouncementException {
        System.out.println(generateNonce());
        return postAux(key, message, announcs, false);
    }
    
    public int postGeneral(PublicKey key, String message, int[] announcs) throws  MessageTooBigException, UserNotRegisteredException,
            InvalidPublicKeyException, InvalidAnnouncementException {
        return postAux(key, message, announcs, true);
    }

    //////////////////////////////////////////////////
    //				      READ						//
    //////////////////////////////////////////////////

    public JSONObject read(PublicKey key, int number) throws InvalidPostsNumberException, UserNotRegisteredException,
    		InvalidPublicKeyException, TooMuchAnnouncementsException {

    	Request request = new Request("READ", key, number);

        try {
            Response response = sendReceive(request);
			checkRead(response);
            return response.getJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject readGeneral(int number) throws InvalidPostsNumberException, TooMuchAnnouncementsException {
    	
    	Request request = new Request("READGENERAL", number);

        try {
            Response response = sendReceive(request);
			checkReadGeneral(response);
            return response.getJsonObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
