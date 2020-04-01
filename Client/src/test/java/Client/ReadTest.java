package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class ReadTest extends BaseTest{


	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException,
			MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {

		clientEndpoint1.register();
		clientEndpoint2.register();
		clientEndpoint1.post("message1 user1", null);
		clientEndpoint1.post("message2 user1", null);
		clientEndpoint1.post("message3 user1", null);

		clientEndpoint2.post("message1 user2", null);
		clientEndpoint2.post("message2 user2", null);
		clientEndpoint2.post("message3 user2", null);


	}
	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_1Post() throws InvalidPostsNumberException, UserNotRegisteredException,
			InvalidPublicKeyException, TooMuchAnnouncementsException {

		String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 1));

		assertEquals("message3 user1", result[0]);
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_3Post_2Users() throws InvalidPostsNumberException, UserNotRegisteredException,
			InvalidPublicKeyException, TooMuchAnnouncementsException  {

		String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 2));
		String[] result2 = getMessagesFromJSON(clientEndpoint2.read("user2", 2));

		assertEquals(result1[0], "message3 user1");
		assertEquals(result1[1], "message2 user1");

		assertEquals(result2[0], "message3 user2");
		assertEquals(result2[1], "message2 user2");

	}


	/*@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_With_SmallerKey() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		clientEndpoint1.read(generateSmallerKey(), 1, privateKey1);
	}*/

	/*@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserNotRegistered() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		//user 3 is not registered
		clientAPI.read(publicKey3, 1, privateKey3);
	}*/

	@Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_BadPostsNumber() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		clientEndpoint1.read("user1", -301);
	}

	@Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_AskingAlotOfPosts() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		//There are only 2 posts
		clientEndpoint1.read("user1", 685);
	}

	/*@Test
	public void Should_Succeed_When_AskingForAll() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		String[] result1 = getMessagesFromJSON(clientAPI.read(publicKey1, 0, privateKey1));
		String[] result2 = getMessagesFromJSON(clientAPI.read(publicKey2, 0, privateKey2));

		assertEquals(result1[0], "message3 user1");
		assertEquals(result1[1], "message2 user1");
		assertEquals(result1[2], "message1 user1");

		assertEquals(result2[0], "message3 user2");
		assertEquals(result2[1], "message2 user2");
		assertEquals(result2[2], "message1 user2");

	}*/

}
