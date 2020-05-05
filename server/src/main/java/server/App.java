package server;

import java.io.IOException;
import java.net.ServerSocket;

public class App 
{
	private static final int PORT = 9000;

    public static void main( String[] args ) {

		// Check if arguments are being wrongly used (should only receive username, or no arguments at all)
		if(args.length > 1 || args.length == 0) {
			System.out.println("\nWrong way of running Server. Please provide the number of faults the system can support");
		}
		else{
			startNServers(Integer.parseInt(args[0]));
		}
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
