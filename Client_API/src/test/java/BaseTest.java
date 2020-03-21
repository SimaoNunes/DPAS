import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.PublicKey;

import org.junit.*;

import Client_API.ClientAPI;
import Library.Request;

////////////////////////////////////////////////////////////////////
//															      //
//   WARNING: Server must be running in order to run these tests  //
//															      //
////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////
//                                                                //
//     Only 1 user is considered for tests purposes (user1)       //
//                                                                //
////////////////////////////////////////////////////////////////////

public class BaseTest {
	
	static ClientAPI clientAPI;
	static KeyStore keyStore;
	static PublicKey publicKey1;
	static char[] passphrase = "changeit".toCharArray();

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the API that will communicate with the Server
		clientAPI = new ClientAPI();
        try {
        	// Get user1 PublicKey from the KeyStore
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("Keystores/keystore"), passphrase);
            publicKey1 = keyStore.getCertificate("user1").getPublicKey();
            // Register user
            clientAPI.register(publicKey1, "user1");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@AfterClass
	public static void cleanUp() throws UnknownHostException, IOException {
        Socket socket = new Socket("localhost", 9000);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(new Request("DELETEALL", null));
        outputStream.close();
        socket.close();
	}
	
}
