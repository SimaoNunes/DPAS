package client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import library.Envelope;
import library.Request;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;

////////////////////////////////////////////////////////////////////
//															      //
//   WARNING: Server must be running in order to run these tests  //
//															      //
////////////////////////////////////////////////////////////////////


public class BaseTest {
	
	static ClientEndpoint clientEndpoint1;
	static ClientEndpoint clientEndpoint2;
	static ClientEndpoint clientEndpoint3;
	static ClientEndpoint clientEndpointError;
	static KeyStore keyStore;
	static char[] passphrase = "changeit".toCharArray();
	static String server_address = "localhost";

	static final int faults = 1;
	static final int PORT = 9000;

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the ClientEndpoint that will communicate with the Server
		clientEndpoint1 = new ClientEndpoint("user1", server_address, faults);
		clientEndpoint2 = new ClientEndpoint("user2", server_address, faults);
		clientEndpoint3 = new ClientEndpoint("user3", server_address, faults);
		clientEndpointError = new ClientEndpoint("usererror", server_address, faults);
	}

	@AfterClass
	public static void cleanUp() {
		setDropNonceFlag(false);
		setDropOperationFlag(false);
        deleteUsers();
	}

    public static PublicKey generateSmallerKey(){
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

    public static void deleteUsers(){
        int port = PORT;
        int i = 0;
        while( i < (faults*3) + 1){
            try(Socket socket = new Socket("localhost", port++)) {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(new Envelope(new Request("DELETEALL", null)));
                outputStream.close();
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public String[] getMessagesFromJSON(JSONObject json){

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

    public String[] getReferencedAnnouncementsFromJSONResultWith1Post(JSONObject json){

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

    public static void shutDown(){
        Socket socket = null;
        try {
            socket = new Socket(server_address, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Request("SHUTDOWN", null));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setTestFlag(boolean flag){
        Socket socket = null;
        String message = "TEST_FLAG_";
        if(flag){
            message+="TRUE";
        }
        else{
            message+="FALSE";
        }
        try {
            socket = new Socket(server_address, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Envelope(new Request(message, null)));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
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
            socket = new Socket(server_address, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Envelope(new Request(message, null)));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
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
            socket = new Socket(server_address, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Envelope(new Request(message, null)));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
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
            socket = new Socket(server_address, 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Envelope(new Request(message, null)));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
