package Server;

import java.io.*;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.*;

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
    			inStream = new ObjectInputStream(socket.getInputStream());
    			outStream = new ObjectOutputStream(socket.getOutputStream());
    			try {
					System.out.println("User connected.");

					String input = inStream.readUTF();
    				String[] utf = input.split("_");
					String cmd = utf[0];
					int length = Integer.parseInt(utf[1]);
					System.out.println(utf[0]);
					System.out.println(utf[1]);
					System.out.println(input);
					//cmd = (String) inStream.readObject();
					switch(cmd) {
						case "POST":
							post(inStream, length);
							break;
						case "GET":
							send(outStream);
							break;
					}
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
    		}catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

		private void post(ObjectInputStream inStream, int length) {
			System.out.println("POST method");
			byte[] message = new byte[length];
			String m;

			try {
				m = (String) inStream.readObject();
				System.out.println(m);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}


		
			/*try{
				byte[] bytes = example.getBytes("UTF-8");

				File file = new File("./storage");
				FileOutputStream fos = new FileOutputStream(file);
	
				fos.write(bytes);
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}*/
		
		}

		private void send(ObjectOutputStream outStream) {
			System.out.println("SEND method");
			
            Path fileLocation = Paths.get("./storage");
		
			if(!Files.exists(fileLocation)){
                System.out.println("Maninho essa merda nao existe");
			} else{
				try {
					byte[] data = Files.readAllBytes(fileLocation);
                	outStream.write(data);
                	outStream.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}

            }
        }
		
		private void quitUser() {
			System.out.println("User disconnected.");
		}
    }
}
