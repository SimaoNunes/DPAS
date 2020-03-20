import static org.junit.Assert.assertEquals;

import Exceptions.InvalidAnnouncementException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.MessageTooBigException;
import Exceptions.UserNotRegisteredException;
import org.junit.*;

import java.security.*;

////////////////////////////////////////////////////////////////////
//                                                                //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////
//                                                                //
//     Only 1 user is considered for tests purposes (user1)       //
//                                                                //
////////////////////////////////////////////////////////////////////

public class PostTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		assertEquals(1, clientAPI.post(publicKey1, "user1 test message", null));
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsValidArray() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		
	}

	@Test(expected = MessageTooBigException.class)
	public void postFailMessageTooBig() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		clientAPI.post(publicKey1, "Has 256 charsssssssssssssssss" +
				"sssssssssssssssssssssssssssssssssssssssssssssssss" +
				"ssssssssssssssssssssssssssssssssssssssssssssssssss" +
				"ssssssssssssssssssssssssssssssssssssssssssssssssss" +
				"sssssssssssssssssssssssssssssssssssss" +
				"sssssssssssssssssssssssssssssssssssssssss", null);
	}

	@Test(expected = InvalidPublicKeyException.class)
	public void postFailInvalidKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey pub = pair.getPublic();

		clientAPI.post(pub, "This is going to fail", null);

	}

}
