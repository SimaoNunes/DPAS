import org.junit.Test;

import Exceptions.AlreadyRegisteredException;

import java.security.KeyStoreException;
import java.security.PublicKey;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////
//                                                                //
//     Only 1 user is considered for tests purposes (user1)       //
//                                                                //
////////////////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsValidArray() throws AlreadyRegisteredException, KeyStoreException {
	        PublicKey publicKey2;
			publicKey2 = keyStore.getCertificate("user2").getPublicKey();
	        clientAPI.register(publicKey2, "user2");
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException {
		// This user is registered @BeforeClass
        clientAPI.register(publicKey1, "user1");
	}
		
	
}
