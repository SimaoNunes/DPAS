package Client_API;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Scanner;


public class App {

	// private static Scanner myObj;
	// private static ObjectOutputStream outStream;
	// private static ObjectInputStream inStream;

    public static void main( String[] args ) {
        System.out.println( "Hello World from the Client_API!" );        
        /*try{
            Socket socket = new Socket("localhost",9000);
            // Instantiate input and output streams
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());
            // Instantiate scanner
            myObj = new Scanner(System.in);
            // Control variable
            boolean open = true;
            while(open) {
                System.out.println("Enter a message:");
                // Read usewr input
                String message = myObj.nextLine();
                outStream.writeObject(message);
                System.out.println("Message sent!");
                if(message.equals("END")) {
                	open = false;
                }
            }
            System.out.println("Bye!");
            socket.close();  
        } catch(Exception e){
            System.out.println(e);
        }*/
        PublicKey key = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            key = kp.getPublic();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        ClientAPI c = new ClientAPI();
        c.post(key, "AIAIAIAI TESTE", null);
        c.post(key, "AIAIAIAI TESTE2222", null);
        
    }

}
