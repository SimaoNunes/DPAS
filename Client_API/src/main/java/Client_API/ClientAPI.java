package Client_API;

import Library.Request;

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
        try{
            createSocket();
            createOutputStream(socket);
        } catch (Exception e){
            e.printStackTrace();
        }
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

    public void send(ObjectOutputStream outStream, Request request) throws IOException {

        outStream.writeObject(request);

        outStream.flush();

        outStream.close();

    }

    public void post(PublicKey key, String message, int[] announcs) {

        Request request = new Request("POST", key, message, announcs);

        try {
            send(outStream, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        /*public void postGeneral(String message){

        try {
            createSocket();
            createOutputStream(socket);
            //send(outStream, message.getBytes());
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void read(int number){

        try {
            createSocket();
            createOutputStream(socket);
            //send(outStream, BigInteger.valueOf(number).toByteArray());
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }*/

}
