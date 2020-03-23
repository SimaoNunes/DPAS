package Client_API;

import Exceptions.InvalidPublicKeyException;
import Exceptions.UnknownPublicKeyException;
import org.junit.Test;

import Exceptions.AlreadyRegisteredException;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////
//                                                     //
//     Only 1 user is already registered (user1)       //
//                                                     //
/////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsValidArray() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
	        clientAPI.register(publicKey2, "user2");
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		// This user is registered @BeforeClass
        clientAPI.register(publicKey1, "user1");
	}

	@Test(expected = UnknownPublicKeyException.class)
	public void Should_Fail_When_Server_DoesntHave_PubKey() throws KeyStoreException, AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		PublicKey badPub = keyStore.getCertificate("userERROR").getPublicKey();
		clientAPI.register(badPub, "userERROR");

	}
	
}
