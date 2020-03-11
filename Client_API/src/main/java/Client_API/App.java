package Client_API;

import java.io.DataOutputStream;
import java.net.Socket;

import java.util.Scanner; 


public class App 
{
    public static void main( String[] args ) {
        System.out.println( "Hello World from the Client_API!" );

        Scanner myObj = new Scanner(System.in);  // Create a Scanner 
        System.out.println("Enter a message:");
        String message = myObj.nextLine();  // Read user input
        myObj.close();
        
        try{      
            Socket s = new Socket("localhost",9000);  
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
            dout.writeUTF(message);  
            dout.flush();  
            System.out.println("Message sent!");  
            dout.close();  
            s.close();  
        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
