package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.json.simple.JSONObject;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class ReadTest extends BaseTest{


	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		clientEndpoint1.register();
		clientEndpoint2.register();

		clientEndpoint1.post("message1 user1", null); // id = 0
		clientEndpoint1.post("message2 user1", null); // id = 1
		clientEndpoint1.post("message3 user1", null); // id = 2

		clientEndpoint2.post("message1 user2", null); // id = 3
		clientEndpoint2.post("message2 user2", null); // id = 4
		clientEndpoint2.post("message3 user2", null); // id = 5
		
		int[] announcs  = {0,3};
		clientEndpoint1.post("message with references from user1", announcs); // id = 6
		int[] announcs2 = {1,2};
		clientEndpoint2.post("message with references from user2", announcs2); // id = 7
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_1Post() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2)); // will receive 2

		// check the one that corresponds to the post refering null announcements
		assertEquals("message3 user1", result[1]); 
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_4Post_2Users() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 3)); // will receive 3
		String[] result2 = getMessagesFromJSON(clientEndpoint2.read("user2", 3)); // will receive 3
 
		// check the ones that correspond to the posts refering null announcements
		assertEquals(result1[1], "message3 user1");
		assertEquals(result1[2], "message2 user1"); 

		assertEquals(result2[1], "message3 user2");
		assertEquals(result2[2], "message2 user2");

	}

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserNotRegistered() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		//user 3 is not registered
		clientEndpoint3.read("user3", 1);
	}

	@Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_BadPostsNumber() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.read("user1", -301);
	}

	@Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_AskingAlotOfPosts() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		//There are only 2 posts
		clientEndpoint1.read("user1", 685);
	}

	@Test
	public void Should_Succeed_When_AskingForAll() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 0));
		String[] result2 = getMessagesFromJSON(clientEndpoint2.read("user2", 0));

		assertEquals(result1[0], "message with references from user1");
		assertEquals(result1[1], "message3 user1");
		assertEquals(result1[2], "message2 user1");
		assertEquals(result1[3], "message1 user1");

		assertEquals(result2[0], "message with references from user2");
		assertEquals(result2[1], "message3 user2");
		assertEquals(result2[2], "message2 user2");
		assertEquals(result2[3], "message1 user2");
	}

	@Test
	public void Should_Succeed_When_CheckingReferencedAnnouncementsResultWith1Post() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		
		// get announcements with references -> which are the most recent ones (1)
		JSONObject result  = clientEndpoint1.read("user1", 1); 
		JSONObject result2 = clientEndpoint2.read("user2", 1);
		System.out.println(getReferencedAnnouncementsFromJSONResultWith1Post(result));
		System.out.println(getReferencedAnnouncementsFromJSONResultWith1Post(result2));

	}

}
