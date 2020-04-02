package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import org.json.simple.JSONObject;

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
		clientEndpoint1.post("private post1 from user1", null); // id = 0
		clientEndpoint1.post("private post2 from user1", null); // id = 1
        //user 2
        clientEndpoint2.post("private post1 from user2", null); // id = 2
		clientEndpoint2.post("private post2 from user2", null); // id = 3

        // public posts now
        //user 1
        clientEndpoint1.postGeneral("public post1 from user1", null); // id = 4
		clientEndpoint1.postGeneral("public post2 from user1", null); // id = 5
        //user 2      
		clientEndpoint2.postGeneral("public post1 from user2", null); // id = 6
        clientEndpoint2.postGeneral("public post2 from user2", null); // id = 7

        int[] announcs  = {0,3};
		clientEndpoint1.postGeneral("message with references from user1", announcs); // id = 8
		int[] announcs2 = {1,2};
		clientEndpoint2.postGeneral("message with references from user2", announcs2); // id = 9
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
        // should succeed even though the user didn't refer any other announcements when posting
        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(3));
        assertEquals("public post2 from user2", general_result[2]);
    }
    
	@Test
	public void Should_Succeed_When_Asking_For_All() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException {
        
        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(0));

        assertEquals(general_result[0], "message with references from user2");
        assertEquals(general_result[1], "message with references from user1");
        assertEquals(general_result[2], "public post2 from user2");
		assertEquals(general_result[3], "public post1 from user2");
        assertEquals(general_result[4], "public post2 from user1");
        assertEquals(general_result[5], "public post1 from user1");
	}
    
    @Test
	public void Should_Succeed_When_CheckingReferencedAnnouncements() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		
		// get announcements with references -> which are the 2 most recent ones (2)
        JSONObject result  = clientEndpoint1.readGeneral(2); 

		// falta fazer read das referencias
		
		// System.out.println(getReferencedAnnouncementsFromJSON(result));

	}


}
