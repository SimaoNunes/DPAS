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

public class PostGeneralTest extends BaseTest {

	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.register();
		clientEndpoint2.register();
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		assertEquals(1, clientEndpoint1.post("user1 test message", null, true));
		assertEquals(1, clientEndpoint2.post("user2 test message", null, true));
	}

	@Test
	public void Should_Succeed_When_ReferenceExistingAnnounce() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		clientEndpoint1.post("user1 test message", null, true);
		clientEndpoint2.post("user2 test message", null, true);
		
		int[] announcs1 = {0};
		int[] announcs2 = {0,1};

		assertEquals(1, clientEndpoint1.post("user1 test message", announcs1, true));
		assertEquals(1, clientEndpoint2.post("user2 test message", announcs2, true));
	}
	
	@Test(expected = InvalidAnnouncementException.class)
	public void Should_Fail_When_AnnouncDoesntExist() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		int[] announcs1 = {20};
		assertEquals(1, clientEndpoint1.post("user1 test message", announcs1, true));
	}

	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.post("Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null, true);
	}
	
	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint3.post("I am not a registered user", null, true);
	}

}
