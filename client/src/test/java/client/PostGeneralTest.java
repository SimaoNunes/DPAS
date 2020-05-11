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
		assertEquals(1, clientEndpoint1.postGeneral("user1 general message1", null));
		assertEquals(1, clientEndpoint2.postGeneral("user2 general message1", null));
	}

	@Test
	public void Should_Succeed_When_ReferenceExistingAnnounce() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.postGeneral("user1 general message2", null);
		clientEndpoint2.postGeneral("user2 general message2", null);
		
		int[] announcs1 = {0};
		int[] announcs2 = {0,1};

		assertEquals(1, clientEndpoint1.postGeneral("user1 general message3", announcs1));
		assertEquals(1, clientEndpoint2.postGeneral("user2 general message3", announcs2));
	}
	
	@Test(expected = InvalidAnnouncementException.class)
	public void Should_Fail_When_AnnouncDoesntExist() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		int[] announcs1 = {20};
		assertEquals(1, clientEndpoint1.postGeneral("user 1 general message4", announcs1));
	}

	@Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.postGeneral("Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
									"ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null);
	}
	
	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint3.postGeneral("I am not a registered user", null);
	}

}
