package Client_API;

import Exceptions.*;
import Library.Request;
import Library.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;

import javax.swing.plaf.InsetsUIResource;

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
    
    
 //////////////////////////////////////////////////////////////
 //															 //
 //   Methods that check if responses must throw exceptions  //
 //															 //
 //////////////////////////////////////////////////////////////

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
    		InvalidPublicKeyException, MessageTooBigException, InvalidAnnouncementException, InvalidPostsNumberException {

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

        }

    }

    
 ///////////////////////
 //					  //
 //   API Functions   //
 //	  	     		  //
 ///////////////////////

    public void register(PublicKey key, String name) throws AlreadyRegisteredException {

        Request request = new Request("REGISTER", key, name);
        // Send request to Server
        try {

            Response response = sendReceive(request);
            if(!response.getSuccess()){
                int error = response.getErrorCode();
                if(error == -2){
                    throw new AlreadyRegisteredException("User with that public key already registered!");
                }
            }
           // On success return 1
        } catch (IOException | ClassNotFoundException e) {
        	// On failure return 0
            e.printStackTrace();
        }
    }

    public int post(PublicKey key, String message, int[] announcs) throws MessageTooBigException, UserNotRegisteredException,
    		InvalidPublicKeyException, InvalidAnnouncementException {

        Request request = new Request("POST", key, message, announcs);
        // Send request to Server
        try {
            Response response = sendReceive(request);
            // Check if response is bad (throws exception in case it is bad)
            checkPost(response);
            // On success, return 1
            return 1;
            
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return 0
            e.printStackTrace();
            return 0;
        }
    }
    
    public int postGeneral(PublicKey key, String message, int[] announcs) throws UserNotRegisteredException,
            InvalidPublicKeyException, MessageTooBigException, InvalidAnnouncementException {

        Request request = new Request("POSTGENERAL", key, message, announcs);
        // Send request to Server
        try {
           Response response = sendReceive(request);
           // Check if response is bad (throws exception in case it is bad)
           checkPost(response);
           // On success, return 1
           return 1;

           // On success return 1
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return 0
            e.printStackTrace();
            return 0;
        }
    }

    public String read(PublicKey key, int number) throws InvalidAnnouncementException,
            InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException {

        Request request = new Request("READ", key, number);
        // Send request to Server
        try {
           Response response = sendReceive(request);
           // Check if response is bad (throws exception in case it is bad)
           checkRead(response);
           // On success, return message
           return response.getMessage();
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return null
            e.printStackTrace();
            return null;
        }
    }

    public String readGeneral(int number) throws InvalidAnnouncementException,
            InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException {

        Request request = new Request("READGENERAL", number);
        // Send request to Server
        try {
            Response response = sendReceive(request);
            // Check if response is bad (throws exception in case it is bad)
            checkRead(response);
            // On success, return message
            return response.getMessage();
        } catch (IOException | ClassNotFoundException e) {
        	// On failure, return null
            e.printStackTrace();
            return null;
        }
    }
}
