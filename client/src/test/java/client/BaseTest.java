package client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import library.Envelope;
import library.Request;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;

////////////////////////////////////////////////////////////////////
//
//   WARNING: Server must be running in order to run these tests
//
////////////////////////////////////////////////////////////////////


public class BaseTest {
	
	static ClientEndpoint clientEndpoint1;
	static ClientEndpoint clientEndpoint2;
	static ClientEndpoint clientEndpoint3;
	static ClientEndpoint clientEndpointError;
	static KeyStore keyStore;
	static char[] passphrase = "changeit".toCharArray();
	static String serverAddress = "localhost";

	static final int faults = 1;
	static final int PORT = 9000;

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the ClientEndpoint that will communicate with the Server
		clientEndpoint1 = new ClientEndpoint("user1");
		clientEndpoint2 = new ClientEndpoint("user2");
		clientEndpoint3 = new ClientEndpoint("user3");
		clientEndpointError = new ClientEndpoint("usererror");
	}

	@AfterClass
	public static void cleanUp() {
        deleteUsers();
	}

    public static PublicKey generateSmallerKey() {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair pair = keyGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();
            return publicKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteUsers() {
        int port = PORT;
        int i = 0;
        while(i < (faults*3) + 1) {
            try(Socket socket = new Socket("localhost", port++)) {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream.writeObject(new Envelope(new Request("DELETEALL")));
				Envelope confirmDelete = (Envelope) inputStream.readObject();
                outputStream.close();
                i++;
            } catch (
            		IOException |
            		ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getMessagesFromJSON(JSONObject json) {

        JSONArray array = (JSONArray) json.get("announcementList");
        String[] result = new String[array.size()];

        int i = 0;
        for (Object object : array) {
            JSONObject obj = (JSONObject) object;
            String msg = (String) obj.get("message");
            result[i++] = msg;
        }

        return result;
    }

    public String[] getMessagesFromJSONGeneral(JSONObject json) {

        JSONArray array = (JSONArray) json.get("announcementList");
        String[] result = new String[array.size()];

        int i = 0;
        for (Object object : array) {
            JSONObject obj = (JSONObject) object;
            JSONObject message = (JSONObject) obj.get("message");
            result[i++] = (String) message.get("message");
        }

        return result;
    }

    public String[] getReferencedAnnouncementsFromJSONResultWith1Post(JSONObject json) {

    	JSONArray arrayAnnouncement = (JSONArray) json.get("announcementList");
        String[] numbers = null;
        JSONArray refs = null;
        
        int i = 0;
        for (Object post : arrayAnnouncement) {
            JSONObject obj = (JSONObject) post;
            refs = (JSONArray) obj.get("ref_announcements");
            numbers = new String[refs.size()];
            for (Object ref : refs) {
                String refString = (String) ref;
                numbers[i++] = refString;
            }
        }
        // Deal with the case of no refs
        if (refs == null) {
            return new String[0]; // empty list
        } else {
        	return numbers;	
        }
    }

    public String[] getReferencedAnnouncementsFromJSONResultWith1PostGeneral(JSONObject json){

        JSONArray arrayAnnouncement = (JSONArray) json.get("announcementList");
        String[] numbers = null;
        JSONArray refs = null;

        int i = 0;
        for (Object post : arrayAnnouncement) {
            JSONObject obj = (JSONObject) ((JSONObject) post).get("message");
            refs = (JSONArray) obj.get("ref_announcements");
            numbers = new String[refs.size()];
            for (Object ref : refs) {
                String refString = (String) ref;
                numbers[i++] = refString;
            }
        }
        // Deal with the case of no refs
        if (refs == null) {
            return new String[0]; // empty list
        } else {
            return numbers;
        }
    }

    public static void shutDown(){
        Socket socket = null;
        try {
            socket = new Socket(serverAddress, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Request("SHUTDOWN"));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setReplayFlag(boolean flag){
        Socket socket = null;
        String message = "REPLAY_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            socket = new Socket(serverAddress, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(new Envelope(new Request(message)));
            Envelope confirmIntegrity = (Envelope) inputStream.readObject();
            outputStream.close();
            socket.close();

        } catch (IOException |
                ClassNotFoundException e) {
        e.printStackTrace();
        }
    }

    public static void setIntegrityFlag(boolean flag){
        Socket socket = null;
        String message = "INTEGRITY_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            socket = new Socket(serverAddress, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(new Envelope(new Request(message)));
            Envelope confirmIntegrity = (Envelope) inputStream.readObject();
            outputStream.close();
            socket.close();

        } catch (IOException |
                ClassNotFoundException e) {
        e.printStackTrace();
        }
    }
    
    public static void setDropNonceFlag(boolean flag){
        Socket socket = null;
        String message = "DROP_NONCE_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            int port = PORT;
            int i = 0;
            while(i < 4){
                socket = new Socket(serverAddress, port + i);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(new Envelope(new Request(message)));
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Envelope confirmDelete = (Envelope) inputStream.readObject();
                inputStream.close();
                outputStream.close();
                socket.close();
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void setDropOperationFlag(boolean flag){
        Socket socket = null;
        String message = "DROP_OPERATION_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            int port = PORT;
            int i = 0;
            while(i < 4){
                socket = new Socket(serverAddress, port + i);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream.writeObject(new Envelope(new Request(message)));
                Envelope confirmDelete = (Envelope) inputStream.readObject();
                inputStream.close();
                outputStream.close();
                socket.close();
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setAtomicWriteFlag(boolean flag){
        Socket socket = null;
        String message = "ATOMIC_WRITE_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            int port = PORT;
            int i = 0;
            while(i < 4){
                socket = new Socket(serverAddress, port + i);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream.writeObject(new Envelope(new Request(message)));
                Envelope confirmDelete = (Envelope) inputStream.readObject();
                inputStream.close();
                outputStream.close();
                socket.close();
                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setGeneralConcurrentWriteFlag(boolean flag){
        String message = "CONCURRENT_WRITE_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        int port = PORT;
        int i = 0;
        while(i < 4){
            try {
                Socket socket = new Socket(serverAddress, port + i);
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(new Envelope(new Request(message)));
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Envelope confirmDelete = (Envelope) inputStream.readObject();
                i++;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
    
}
