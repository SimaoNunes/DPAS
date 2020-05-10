package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//
//   WARNING: Server must be running in order to run these tests
//
////////////////////////////////////////////////////////////////////

public class PostTest extends BaseTest {

	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.register();
		clientEndpoint2.register();
	}
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		assertEquals(1, clientEndpoint1.post("user1 announc message1", null, false));
		assertEquals(1, clientEndpoint2.post("user2 announc message1", null, false));
	}
	
	@Test
	public void Should_Succeed_When_ReferenceExistingAnnounce() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		
		assertEquals(1, clientEndpoint1.post("user1 announc message2", null, false));
		assertEquals(1, clientEndpoint2.post("user2 announc message2", null, false));

		int[] announcs1 = {0};
		int[] announcs2 = {0,1};

		assertEquals(1, clientEndpoint1.post("user1 announc message3", announcs1, false));
		assertEquals(1, clientEndpoint2.post("user2 announc message3", announcs2, false));
	}
	
	@Test(expected = InvalidAnnouncementException.class)
	public void Should_Fail_When_AnnouncDoesntExist() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		int[] announcs1 = {20};
		assertEquals(1, clientEndpoint1.post("user1 announc message4", announcs1, false));
	}

	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.post("Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null, false);
	}

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint3.post("I am not a registered user", null, false);
	}
	
}
