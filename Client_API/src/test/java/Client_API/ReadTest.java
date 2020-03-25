package Client_API;

import static org.junit.Assert.assertEquals;

import Exceptions.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyStoreException;
import java.security.PublicKey;

public class ReadTest extends BaseTest{


	@BeforeClass
	public static void populate() throws InvalidAnnouncementException,
			UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException, UnknownPublicKeyException, AlreadyRegisteredException {

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
	public void Should_Succeed_When_AnnouncsIsNull_With_1_Post() throws InvalidAnnouncementException, InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException, TooMuchAnnouncementsException {

		String[] result = getMessagesFromJSON(clientAPI.read(publicKey1, 1));

		assertEquals("message3 user1", result[0]);
	}

	@Test
	public void Should_Succeed_When_AnnouncsIsNull_With_3_Post_2_Users() throws UserNotRegisteredException,
			InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {

		String[] result1 = getMessagesFromJSON(clientAPI.read(publicKey1, 2));
		String[] result2 = getMessagesFromJSON(clientAPI.read(publicKey2, 2));

		assertEquals(result1[0], "message3 user1");
		assertEquals(result1[1], "message2 user1");

		assertEquals(result2[0], "message3 user2");
		assertEquals(result2[1], "message2 user2");

	}


	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_With_Smaller_Key() throws UserNotRegisteredException,
			InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		clientAPI.read(generateSmallerKey(), 1);
	}

	@Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_User_Not_Registered() throws UserNotRegisteredException,
			InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		//user 3 is not registered
		clientAPI.read(publicKey3, 1);
	}

	@Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_Bad_Posts_Number() throws UserNotRegisteredException,
			InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		clientAPI.read(publicKey1, -301);
	}

	@Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_Asking_Alot_Of_Posts() throws UserNotRegisteredException, InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		//There are only 2 posts
		clientAPI.read(publicKey1, 685);
	}

	@Test
	public void Should_Succeed_When_Asking_For_All() throws InvalidAnnouncementException,
			UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException, TooMuchAnnouncementsException, InvalidPostsNumberException {
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
