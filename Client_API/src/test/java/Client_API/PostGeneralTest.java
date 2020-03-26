package Client_API;

import static org.junit.Assert.assertEquals;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class PostGeneralTest extends BaseTest {

	@BeforeClass
	public static void populate() throws AlreadyRegisteredException,
			UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(publicKey1, "user1");
		clientAPI.register(publicKey2, "user2");
	}
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		assertEquals(1, clientAPI.postGeneral(publicKey1, "user1 test message", null));
		assertEquals(1, clientAPI.postGeneral(publicKey2, "user2 test message", null));

	}

	/*@Test
	public void Should_Succeed_When_AnnouncsIsValidArray() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		
	}*/

	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		clientAPI.postGeneral(publicKey1, "Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
										  "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
										  "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
										  "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null);
	}

	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_KeyIsInvalid() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey pub = pair.getPublic();

		clientAPI.postGeneral(pub, "This is going to fail", null);

	}
	
	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException {
		clientAPI.post(publicKey3, "I am not a registered user", null);
	}

}
