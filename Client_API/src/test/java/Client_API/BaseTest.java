package Client_API;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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
	public static void cleanUp() throws UnknownHostException, IOException {
        Socket socket = new Socket("localhost", 9000);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new Request("DELETEALL", null));
        outputStream.close();
        socket.close();
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
	
}
