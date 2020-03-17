package Client_API;

import Exceptions.MessageTooBigException;
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

    private static Socket socket;
    private static Scanner scanner;
    private static ObjectOutputStream outStream;
    private static ObjectInputStream inStream;

    public ClientAPI(){

    }

    public void createSocket() throws IOException {
        socket = new Socket("localhost",9000);
    }

    public void createOutputStream(Socket socket) throws IOException {
        outStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public void createInputStream(Socket socket) throws IOException {
        inStream = new ObjectInputStream(socket.getInputStream());
    }

    public void createScanner(){
        scanner = new Scanner(System.in);
    }

    public void send(Request request) throws IOException {
        createOutputStream(socket);

        outStream.writeObject(request);
    }

    public void register(PublicKey key, String name){

        Request request = new Request("REGISTER", key, name);
        // Send request to Server
        try {
           send(request);
           // On success return 1
        } catch (IOException e) {
        	// On failure return 0
            e.printStackTrace();
        }
    }

    public void post(PublicKey key, String message, int[] announcs) {

        Request request = new Request("POST", key, message, announcs);
        // Send request to Server
        try {
            createSocket();
            send(request);
            createInputStream(socket);
            Response response = (Response) inStream.readObject();
            MessageTooBigException mtb = (MessageTooBigException) response.getException();
            System.out.println(mtb.getMessage());
            // On success return 1
            
        } catch (IOException e) {
        	// On failure return 0
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void postGeneral(PublicKey key, String message, int[] announcs){

        Request request = new Request("POSTGENERAL", key, message, announcs);
        // Send request to Server
        try {
           send(request);
           // On success return 1
        } catch (IOException e) {
        	// On failure return 0
            e.printStackTrace();
        }
    }

    public void read(PublicKey key, int number){

        Request request = new Request("READ", key, number);
        // Send request to Server
        try {
           send(request);
           // On success return 1
        } catch (IOException e) {
        	// On failure return 0
            e.printStackTrace();
        }
    }

    public void readGeneral(int number){

        Request request = new Request("READGENERAL", number);
        // Send request to Server
        try {
           send(request);
           // On success return 1
        } catch (IOException e) {
        	// On failure return 0
            e.printStackTrace();
        }
    }
    
    public int always1(int number) {
    	return 1;
    }
}
