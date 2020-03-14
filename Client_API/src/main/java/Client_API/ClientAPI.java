package Client_API;

import Library.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Scanner;

public class ClientAPI {

    private static Scanner scanner;
    private static ObjectOutputStream outStream;
    private static ObjectInputStream inStream;

    public ClientAPI(){
    }

    public Socket createSocket() throws IOException {
        return new Socket("localhost",9000);
    }

    public ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    public ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }

    public Scanner createScanner(){
        return new Scanner(System.in);
    }

    public void send(ObjectOutputStream outStream, Request request) throws IOException {

        outStream.writeObject(request);

        outStream.flush();

        outStream.close();

    }

    public void post(PublicKey key, String message, int[] announcs){

        Request request = new Request("POST", key, message, announcs);

        try {
            Socket socket = createSocket();
            outStream = createOutputStream(socket);
            send(outStream, request);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void postGeneral(String message){

        try {
            Socket socket = createSocket();
            outStream = createOutputStream(socket);
            //send(outStream, message.getBytes());
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void read(int number){

        try {
            Socket socket = createSocket();
            outStream = createOutputStream(socket);
            //send(outStream, BigInteger.valueOf(number).toByteArray());
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
