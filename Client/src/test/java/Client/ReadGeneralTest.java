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

public class ReadGeneralTest extends BaseTest{

    @BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, MessageTooBigException,
												 UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		clientEndpoint1.register();
        clientEndpoint2.register();
    
        // some private posts first, to test order of posting
        //user 1      
		clientEndpoint1.post("private post1 from user1", null);
		clientEndpoint1.post("private post2 from user1", null);
        //user 2
        clientEndpoint2.post("private post1 from user2", null);
		clientEndpoint2.post("private post2 from user2", null);

        // public posts now
        //user 1
        clientEndpoint1.postGeneral("public post1 from user1", null);
		clientEndpoint1.postGeneral("public post2 from user1", null);
        //user 2      
		clientEndpoint2.postGeneral("public post1 from user2", null);
		clientEndpoint2.postGeneral("public post2 from user2", null);
    }
    
    
    @Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_Bad_Posts_Number() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException {
		clientEndpoint1.readGeneral(-301);
    }
    
    @Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_Asking_Alot_Of_Posts() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException {
		// There are only 4 posts
		clientEndpoint1.readGeneral(685);
	}

    @Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException {
        // get most recent general post -> should succeed even though the user didn't refer any other announcements when posting
        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(1));
        assertEquals("public post2 from user2", general_result[0]);
    }
    
	@Test
	public void Should_Succeed_When_Asking_For_All() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException {
        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(0));

		assertEquals(general_result[0], "public post2 from user2");
		assertEquals(general_result[1], "public post1 from user2");
        assertEquals(general_result[2], "public post2 from user1");
        assertEquals(general_result[3], "public post1 from user1");
	}
	
}
