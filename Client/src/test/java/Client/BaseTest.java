package Client;

import Client.ClientEndpoint;
import Library.Envelope;
import Library.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the ClientEndpoint that will communicate with the Server
		clientEndpoint1 = new ClientEndpoint("user1");
		clientEndpoint2 = new ClientEndpoint("user2");
		clientEndpoint3 = new ClientEndpoint("user3");
		clientEndpointError = new ClientEndpoint("usererror");
	}

	@AfterClass
	public static void cleanUp(){
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
        Socket socket = null;
        try {
            socket = new Socket("localhost", 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Envelope(new Request("DELETEALL", null)));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
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

    public int[] getReferencedAnnouncementsFromJSON(JSONObject json){

        System.out.println(json);
        JSONArray array = (JSONArray) json.get("ref_announcements");
        System.out.println("brooooo");

        System.out.println(array);
    
        // Deal with the case of a non-array value
        if (array == null) {
            return new int[] {}; // empty list
        }

        // Create an int array to accomodate the numbers
        int[] numbers = new int[array.size()];

        // Extract numbers from JSON array
        for (int i = 0; i < array.size(); ++i) {
            numbers[i] = (int) array.get(i);
        }

        return numbers;
    }

    public static void shutDown(){
        Socket socket = null;
        try {
            socket = new Socket("localhost", 9000);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(new Request("SHUTDOWN", null));
            outputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
