package Server;

import java.io.*;

import java.security.PublicKey;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.net.ServerSocket;
import java.net.Socket;

import Library.Request;
import jdk.nashorn.internal.ir.RuntimeNode;
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

					//aqui decifra-se

					Request request = (Request) inStream.readObject();
					switch(request.getOperation()) {
						case "POST":
							post(request);
							break;
						case "READ":
							read(outStream);
							break;
						case "POSTGENERAL":
							postGeneral();
							break;
						case "READGENERAL":
							readGeneral();
							break;
					}
					socket.close();
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
    		}catch (Exception e) {
    			e.printStackTrace();
    		}
    	}

    	private void readGeneral(){

		}

    	private void postGeneral(){

		}

		private void post(Request request) throws IOException {
			System.out.println("POST method");

			int nAnnouncements = 0;
			String path = "./storage/username/";
			// MAIS TARDE TEMOS DE SUBSTITUIR ^USERNAME PELO ID DO USER
			File files = new File(path);

			if (!files.exists()) {
				files.mkdirs();
				System.out.println("Directories created!");
			} else {
				nAnnouncements = files.list().length;
			}
			System.out.println("Total announcements" + Integer.toString(nAnnouncements));

			String announcement = request.getMessage();
			byte[] bytesToStore = announcement.getBytes();

			try{
				System.out.println("Inside Try");
				File file = new File(path + Integer.toString(nAnnouncements));
				FileOutputStream fos = new FileOutputStream(file);

				fos.write(bytesToStore);
				fos.close();

			} catch (Exception e){
				e.printStackTrace();
			}
    	}

		private void read(ObjectOutputStream outStream) {
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
