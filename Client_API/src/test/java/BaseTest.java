import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;

import org.junit.*;

import Client_API.ClientAPI;

public class BaseTest {
	
	static ClientAPI clientAPI;
	static PublicKey publicKey;

	@BeforeClass
	public static void oneTimeSetup() {
		clientAPI = new ClientAPI();
        KeyStore ks;
        publicKey = null;
        char[] passphrase = "changeit".toCharArray();
        try {

            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("Keystores/keystore"), passphrase);
            Key key = ks.getKey("user1", passphrase);

            publicKey = ks.getCertificate("user1").getPublicKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@AfterClass
	public static void cleanup() {

	}
	
}
