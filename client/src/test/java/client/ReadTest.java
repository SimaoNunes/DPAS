package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.json.simple.JSONObject;

////////////////////////////////////////////////////////////////////
//
//   WARNING: Server must be running in order to run these tests
//
////////////////////////////////////////////////////////////////////

public class ReadTest extends BaseTest{


	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		clientEndpoint1.register();
		clientEndpoint2.register();

		clientEndpoint1.post("user1 announc message1", null); // id = 0
		clientEndpoint1.post("user1 announc message2", null); // id = 1
		clientEndpoint1.post("user1 announc message3", null); // id = 2

		clientEndpoint2.post("user2 announc message1", null); // id = 3
		clientEndpoint2.post("user2 announc message2", null); // id = 4
		clientEndpoint2.post("user2 announc message3", null); // id = 5
		
		int[] announcs  = {0,3};
		clientEndpoint1.post("user1 announc message4", announcs); // id = 6
		int[] announcs2 = {1,4};
		clientEndpoint2.post("user2 announc message4", announcs2); // id = 7

	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_1Post() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2)); // will receive 2
		// check the one that corresponds to the post referring null announcements
		assertEquals("user1 announc message3", result[1]); 
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_4Post_2Users() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 3)); // will receive 3
		String[] result2 = getMessagesFromJSON(clientEndpoint2.read("user2", 3)); // will receive 3

		// check the ones that correspond to the posts referring null announcements
		assertEquals(result1[1], "message3 user1");
		assertEquals(result1[2], "message2 user1"); 

		assertEquals(result2[1], "message3 user2");
		assertEquals(result2[2], "message2 user2");

	}

	@Test
	public void Should_Succeed_When_UserReadingNotRegistered() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		//user 3 is not registered
		String[] result1 = getMessagesFromJSON(clientEndpoint2.read("user1", 3)); // will receive 3
		assertEquals(result1[1], "message3 user1");
	}
	
	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserToReadFromNotRegistered() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		//user 3 is not registered
		clientEndpoint2.read("user3", 1);
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

		assertEquals(result1[0], "user1 announc message4");
		assertEquals(result1[1], "user1 announc message3");
		assertEquals(result1[2], "user1 announc message2");
		assertEquals(result1[3], "user1 announc message1");

		assertEquals(result2[0], "user2 announc message4");
		assertEquals(result2[1], "user2 announc message3");
		assertEquals(result2[2], "user2 announc message2");
		assertEquals(result2[3], "user2 announc message1");
	}

	@Test
	public void Should_Succeed_When_CheckingReferencedAnnouncementsResultWith1Post() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		
		// get announcements with references -> which are the most recent ones for each user (1)
		JSONObject result1 = clientEndpoint1.read("user1", 1); 
		JSONObject result2 = clientEndpoint2.read("user2", 1);

		int ref_id1_from_user1 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result1)[0]);
		int ref_id2_from_user1 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result1)[1]);

		assertEquals(0, ref_id1_from_user1);
		assertEquals(3, ref_id2_from_user1);

		int ref_id1_from_user2 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result2)[0]);
		int ref_id2_from_user2 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result2)[1]);

		assertEquals(1, ref_id1_from_user2);
		assertEquals(4, ref_id2_from_user2);

	}

}
