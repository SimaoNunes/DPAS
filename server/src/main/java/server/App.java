package server;

import java.io.IOException;
import java.net.ServerSocket;

public class App 
{
	private static final int PORT = 9000;

    public static void main( String[] args ) {
    	startNServers(1);
	}

	private static void startNServers(int faults){
		int port 		  = PORT;
		int nServers      = 0;
		int totalReplicas = 3 * faults + 1;

        while (nServers < totalReplicas){  // N > 3f
    		try{
				new Server(new ServerSocket(port), port);
				port++;
				nServers++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
