package Server;

import java.io.DataInputStream;

import java.net.ServerSocket;
import java.net.Socket;

public class App 
{
    public static void main( String[] args ) {
        System.out.println( "Hello World from server!" );

        try{  
            ServerSocket ss = new ServerSocket(9000);  
            Socket s = ss.accept();

            DataInputStream dis = new DataInputStream(s.getInputStream());  
            String str = (String)dis.readUTF();  
            System.out.println("message = "+str);  
            
            ss.close();  
        } catch(Exception e) {
            System.out.println("Something went wrong.");
        }  
    }
}
