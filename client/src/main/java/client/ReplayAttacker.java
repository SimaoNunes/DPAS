package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

import library.Envelope;

public class ReplayAttacker {
	
	private String serverAddress = null;

	/***** Constructor *****/
    public ReplayAttacker(String serverAddress){
        this.serverAddress = serverAddress;
    }
    
    /***** Getters and Setters *****/
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    /***** Connection auxiliary methods *****/
    private ObjectOutputStream createOutputStream(Socket socket) throws IOException {
        return new ObjectOutputStream(socket.getOutputStream());
    }

    private ObjectInputStream createInputStream(Socket socket) throws IOException {
        return new ObjectInputStream(socket.getInputStream());
    }
    
    private Socket createSocket() throws IOException {
        return new Socket(getServerAddress(), 9000);
    }
    
    /***** Replay attack method *****/
    public void sendReplays(Envelope envelope, int n_replays) {
        try {
            int i = 0;
            while(i < n_replays){
                Socket socket = createSocket();
                createOutputStream(socket).writeObject(envelope);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
