package Client_API;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateException;

import Exceptions.*;
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
	static PublicKey publicKey1;
	static PublicKey publicKey2;
	static PublicKey publicKey3;
	static char[] passphrase = "changeit".toCharArray();

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the API that will communicate with the Server
		clientAPI = new ClientAPI();
        initiate();
		populate();

	}

	@AfterClass
	public static void cleanUp(){
        deleteUsers();
	}

	public static void initiate(){
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("Keystores/keystore"), passphrase);
            publicKey1 = keyStore.getCertificate("user1").getPublicKey();
            publicKey2 = keyStore.getCertificate("user2").getPublicKey();
            publicKey3 = keyStore.getCertificate("user3").getPublicKey();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public static void populate(){
        try {
            clientAPI.register(publicKey1, "user1");
            clientAPI.post(publicKey1, "This is a test message for user3 to read", null);
        } catch (Exception e) {
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
	
}
