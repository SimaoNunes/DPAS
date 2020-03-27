package Client_API;

import static org.junit.Assert.assertEquals;

import Exceptions.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyStoreException;
import java.security.PublicKey;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class ReadGeneralTest extends BaseTest{

    @BeforeClass
	public static void populate() throws InvalidAnnouncementException,
			UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException, UnknownPublicKeyException, AlreadyRegisteredException {

		clientAPI.register(publicKey1, "user1");
        clientAPI.register(publicKey2, "user2");
    
        // some private posts first, to test order of posting
        //user 1      
		clientAPI.post(publicKey1, "private post1 from user1", null);
		clientAPI.post(publicKey1, "private post2 from user1", null);
        //user 2
        clientAPI.post(publicKey2, "private post1 from user2", null);
		clientAPI.post(publicKey2, "private post2 from user2", null);

        // public posts now
        //user 1
        clientAPI.postGeneral(publicKey1, "public post1 from user1", null);
		clientAPI.postGeneral(publicKey1, "public post2 from user1", null);
        //user 2      
        clientAPI.postGeneral(publicKey2, "public post1 from user2", null);
        clientAPI.postGeneral(publicKey2, "public post2 from user2", null);
    }
    
    
    @Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_Bad_Posts_Number() throws UserNotRegisteredException,
			InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		clientAPI.readGeneral(-301);
    }
    
    @Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_Asking_Alot_Of_Posts() throws UserNotRegisteredException, InvalidAnnouncementException, InvalidPublicKeyException, MessageTooBigException, InvalidPostsNumberException, TooMuchAnnouncementsException {
		// There are only 4 posts
		clientAPI.readGeneral(685);
	}

    @Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidAnnouncementException, InvalidPostsNumberException, UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException, TooMuchAnnouncementsException {

        // get most recent private post from user 1 -> should succeed even though the user didn't refer any other announcements when posting
        String[] private_result = getMessagesFromJSON(clientAPI.read(publicKey1, 1));
        assertEquals("private post2 from user1", private_result[0]);
        
        // get most recent general post -> should succeed even though the user didn't refer any other announcements when posting
        String[] general_result = getMessagesFromJSON(clientAPI.readGeneral(1));
        assertEquals("public post2 from user2", general_result[0]);
    }
    
    
	@Test
	public void Should_Succeed_When_Asking_For_All() throws InvalidAnnouncementException,
			UserNotRegisteredException, MessageTooBigException, InvalidPublicKeyException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        
        String[] general_result = getMessagesFromJSON(clientAPI.readGeneral(0));

		assertEquals(general_result[0], "public post2 from user2");
		assertEquals(general_result[1], "public post1 from user2");
        assertEquals(general_result[2], "public post2 from user1");
        assertEquals(general_result[3], "public post1 from user1");
        
	}
}
