package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//                                                                //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

public class PostTest extends BaseTest {

	@BeforeClass
	public static void populate() throws AlreadyRegisteredException,
			UnknownPublicKeyException, InvalidPublicKeyException {
		clientEndpoint1.register();
		clientEndpoint2.register();
	}
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException  {
		assertEquals(1, clientEndpoint1.post("user1 test message", null));
		assertEquals(1, clientEndpoint2.post("user2 test message", null));
	}
	
	@Test
	public void Should_Succeed_When_ReferenceExistingAnnounce() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException {
		int[] announcs1 = {0};
		int[] announcs2 = {0,1,2};
		assertEquals(1, clientEndpoint1.post("user1 test message", announcs1));
		assertEquals(1, clientEndpoint2.post("user2 test message", announcs2));
	}
	
	/*@Test
	public void Should_Fail_When_AnnouncDoesntExist() {
		
	}*/

	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException  {
		clientEndpoint1.post("Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null);
	}

	/*@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_KeyIsInvalid() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAnnouncementException, UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException {

		clientAPI.post(generateSmallerKey(), "This is going to fail", null, privateKey1);
	}*/

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException, InvalidAnnouncementException {
		clientEndpoint3.post("I am not a registered user", null);
	}
	
}
