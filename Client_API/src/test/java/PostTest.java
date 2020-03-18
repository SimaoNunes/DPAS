import static org.junit.Assert.assertEquals;

import Exceptions.InvalidAnnouncementException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.MessageTooBigException;
import Exceptions.UserNotRegisteredException;
import org.junit.*;

import java.security.*;

public class PostTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() {
		//assertEquals(1, clientAPI.post(publicKey, "teste", null));
	}

	@Test
	public void postSuccess() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		assertEquals(1, clientAPI.post(publicKey, "Isto e um teste!", null));
	}

	@Test(expected = MessageTooBigException.class)
	public void postFailMessageTooBig() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		clientAPI.post(publicKey, "tem 256 caracteressssssssssss" +
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

		clientAPI.post(pub, "isto vai correr mal", null);

	}

}
