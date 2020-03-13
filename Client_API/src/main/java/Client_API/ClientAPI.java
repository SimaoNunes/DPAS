package Client_API;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    public void post(String message){
        try {
            int length = message.getBytes().length;
            outStream.writeUTF("POST_" + length);
            outStream.writeObject(message);
            outStream.flush();
            outStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
