package Client_API;

import java.io.DataOutputStream;
import java.net.Socket;


public class App 
{
    public static void main( String[] args ) {
        System.out.println( "Hello World from the Client_API!" );
        try{      
            Socket s = new Socket("localhost",9000);  
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
            dout.writeUTF("Hello Server :p");  
            dout.flush();  
            System.out.println("Message sent!");  
            dout.close();  
            s.close();  
        } catch(Exception e){
            System.out.println(e);
        }  
    }
}
