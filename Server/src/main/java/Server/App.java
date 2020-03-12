package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class App 
{
    public static void main( String[] args ) {

        try{
        	// Start server
        	ServerSocket ss = new ServerSocket(9000);
            System.out.println("Server running on port 9000!");
            // Accept clients and open threads for each one of them
            while (true){
                Socket inSocket = ss.accept();
                ServerThread newServerThread = new ServerThread(inSocket);
				newServerThread.start();
            }
            // ss.close(); agora o cliente so fecha a sua conexao... por enquanto para mandar o server abaixo é mandar um CTRL+C         
        } catch(Exception e) {
            System.out.println("Something went wrong.");
        }  
    }
    
    // Class that is instantiated when a user connects to the server
    static class ServerThread extends Thread {

    	private Socket socket = null;
    	ObjectOutputStream outStream;
    	ObjectInputStream inStream;

    	public ServerThread(Socket clientSocket) {
    		this.socket = clientSocket;
    	}

    	public void run() {

    		try {
    			outStream = new ObjectOutputStream(socket.getOutputStream());
    			inStream = new ObjectInputStream(socket.getInputStream());
    			String cmd = null;
    			Boolean open = true;
    			try {
    				System.out.println("User connected.");
    				while(open) {
    					cmd = (String) inStream.readObject();
    					switch(cmd) {
    						case "POST":
    							post();
    							break;
    						case "END":
    							quitUser();
    							open = false;
    							break;
    					}
    				}
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
    		}catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

		private void post() {
			System.out.println("POST");
		}
		
		private void quitUser() {
			System.out.println("User disconnected.");
		}
    }
}
