package Client_API;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.*;

import Client_API.ClientAPI;
import Library.Request;

////////////////////////////////////////////////////////////////////
//															      //
//   WARNING: Server must be running in order to run these tests  //
//															      //
////////////////////////////////////////////////////////////////////


public class BaseTest {
	
	static ClientAPI clientAPI;
	static KeyStore keyStore;
	static PrivateKey privateKey1;
	static PrivateKey privateKey2;
	static PrivateKey privateKey3;
	static PublicKey publicKey1;
	static PublicKey publicKey2;
	static PublicKey publicKey3;
	static char[] passphrase = "changeit".toCharArray();

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the API that will communicate with the Server
		clientAPI = new ClientAPI();
        initiate();

	}

	@AfterClass
	public static void cleanUp(){
        deleteUsers();
	}

	public static void initiate(){
	        try {
				keyStore = KeyStore.getInstance("JKS");
				keyStore.load(new FileInputStream("Keystores/keystore"), passphrase);
				privateKey1 = (PrivateKey) keyStore.getKey("user1", passphrase);
				privateKey2 = (PrivateKey) keyStore.getKey("user2", passphrase);
				privateKey3 = (PrivateKey) keyStore.getKey("user3", passphrase);
				publicKey1 = keyStore.getCertificate("user1").getPublicKey();
				publicKey2 = keyStore.getCertificate("user2").getPublicKey();
				publicKey3 = keyStore.getCertificate("user3").getPublicKey();
			} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException
					| IOException e) {
				e.printStackTrace();
			}
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
            outputStream.writeObject(new Request("DELETEALL", null));
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
