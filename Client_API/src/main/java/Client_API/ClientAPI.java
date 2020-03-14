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

        createSocket();

        createOutputStream(socket);

        outStream.writeObject(request);

        outStream.flush();

        outStream.close();
    }

    public void register(PublicKey key, String name){

        Request request = new Request("REGISTER", key, name);

        try{
            send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void post(PublicKey key, String message, int[] announcs) {

        Request request = new Request("POST", key, message, announcs);

        try {
            send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        public void postGeneral(PublicKey key, String message, int[] announcs){

            Request request = new Request("POSTGENERAL", key, message, announcs);

            try {
               send(request);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void read(PublicKey key, int number){

        Request request = new Request("READ", key, number);

        try {
            send(request);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void readGeneral(int number){

        Request request = new Request("READGENERAL", number);

        try{
            send(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
