package Client_API;

import static org.junit.Assert.assertEquals;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class ReadTest extends BaseTest{


	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException,
			MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {

		clientAPI.register(publicKey1, "user1");
		clientAPI.register(publicKey2, "user2");
		clientAPI.post(publicKey1, "message1 user1", null);
		clientAPI.post(publicKey1, "message2 user1", null);
		clientAPI.post(publicKey1, "message3 user1", null);

		clientAPI.post(publicKey2, "message1 user2", null);
		clientAPI.post(publicKey2, "message2 user2", null);
		clientAPI.post(publicKey2, "message3 user2", null);


	}
	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_1Post() throws InvalidPostsNumberException, UserNotRegisteredException,
			InvalidPublicKeyException, TooMuchAnnouncementsException {

		String[] result = getMessagesFromJSON(clientAPI.read(publicKey1, 1));

		assertEquals("message3 user1", result[0]);
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_3Post_2Users() throws InvalidPostsNumberException, UserNotRegisteredException,
			InvalidPublicKeyException, TooMuchAnnouncementsException  {

		String[] result1 = getMessagesFromJSON(clientAPI.read(publicKey1, 2));
		String[] result2 = getMessagesFromJSON(clientAPI.read(publicKey2, 2));

		assertEquals(result1[0], "message3 user1");
		assertEquals(result1[1], "message2 user1");

		assertEquals(result2[0], "message3 user2");
		assertEquals(result2[1], "message2 user2");

	}


	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_With_SmallerKey() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		clientAPI.read(generateSmallerKey(), 1);
	}

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserNotRegistered() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		//user 3 is not registered
		clientAPI.read(publicKey3, 1);
	}

	@Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_BadPostsNumber() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		clientAPI.read(publicKey1, -301);
	}

	@Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_AskingAlotOfPosts() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		//There are only 2 posts
		clientAPI.read(publicKey1, 685);
	}

	@Test
	public void Should_Succeed_When_AskingForAll() throws InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, TooMuchAnnouncementsException {
		System.out.println("\n\n\n\nAQUI\n\n\n\n"+clientAPI.read(publicKey1, 0));
		String[] result1 = getMessagesFromJSON(clientAPI.read(publicKey1, 0));
		String[] result2 = getMessagesFromJSON(clientAPI.read(publicKey2, 0));

		assertEquals(result1[0], "message3 user1");
		assertEquals(result1[1], "message2 user1");
		assertEquals(result1[2], "message1 user1");

		assertEquals(result2[0], "message3 user2");
		assertEquals(result2[1], "message2 user2");
		assertEquals(result2[2], "message1 user2");

	}


}
