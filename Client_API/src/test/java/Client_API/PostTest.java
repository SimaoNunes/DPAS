package Client_API;

import static org.junit.Assert.assertEquals;

import Exceptions.*;
import org.junit.*;

import java.security.*;

////////////////////////////////////////////////////////////////////
//                                                                //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

public class PostTest extends BaseTest {

	@BeforeClass
	public static void populate() throws AlreadyRegisteredException,
			UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(publicKey1, "user1");
		clientAPI.register(publicKey2, "user2");
	}
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		assertEquals(1, clientAPI.post(publicKey1, "user1 test message", null));
		assertEquals(1, clientAPI.post(publicKey2, "user2 test message", null));
	}
	
	@Test
	public void Should_Succeed_When_ReferenceExistingAnnounce() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException {
		int[] announcs1 = {0};
		int[] announcs2 = {0,1,2};
		assertEquals(1, clientAPI.post(publicKey1, "user1 test message", announcs1));
		assertEquals(1, clientAPI.post(publicKey2, "user2 test message", announcs2));
	}
	
	/*@Test
	public void Should_Fail_When_AnnouncDoesntExist() {
		
	}*/


	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {
		clientAPI.post(publicKey1, "Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null);
	}

	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_KeyIsInvalid() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {


		clientAPI.post(generateSmallerKey(), "This is going to fail", null);

	}

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException {
		clientAPI.post(publicKey3, "I am not a registered user", null);
	}

	
}
