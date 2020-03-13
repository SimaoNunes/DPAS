package Client_API;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    public void post(String message){

        try {
            Socket socket = createSocket();
            outStream = createOutputStream(socket);
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
