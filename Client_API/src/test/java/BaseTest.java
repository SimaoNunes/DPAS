import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;

import org.junit.*;

import Client_API.ClientAPI;

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

	@BeforeClass
	public static void oneTimeSetup() {
		// Instantiate class to be tested, in this case the API that will communicate with the Server
		clientAPI = new ClientAPI();
		// KeyStore password
        char[] passphrase = "changeit".toCharArray();
        try {
        	// Get user1 PublicKey from the KeyStore
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("Keystores/keystore"), passphrase);
            publicKey1 = keyStore.getCertificate("user1").getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@AfterClass
	public static void cleanup() {

	}
	
}
