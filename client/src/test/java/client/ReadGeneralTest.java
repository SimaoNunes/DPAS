package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import org.json.simple.JSONObject;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//																  //
////////////////////////////////////////////////////////////////////

public class ReadGeneralTest extends BaseTest {

    @BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, MessageTooBigException,
												 UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		clientEndpoint1.register();
        clientEndpoint2.register();
    
        // some private posts first, to test order of posting
        //user 1      
		clientEndpoint1.post("private post1 from user1", null, false); // id = 0
		clientEndpoint1.post("private post2 from user1", null, false); // id = 1
        //user 2
        clientEndpoint2.post("private post1 from user2", null, false); // id = 2
		clientEndpoint2.post("private post2 from user2", null, false); // id = 3

        // public posts now
        //user 1
        clientEndpoint1.post("public post1 from user1", null, true); // id = 4
		clientEndpoint1.post("public post2 from user1", null, true); // id = 5
        //user 2      
		clientEndpoint2.post("public post1 from user2", null, true); // id = 6
        clientEndpoint2.post("public post2 from user2", null, true); // id = 7

        int[] announcs  = {0,3};
		clientEndpoint1.post("message with references from user1", announcs, true); // id = 8
    }
    
    
    @Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_Bad_Posts_Number() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {
		clientEndpoint1.readGeneral(-301);
    }
    
    @Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_Asking_Alot_Of_Posts() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {
		// There are only 4 posts
		clientEndpoint1.readGeneral(685);
	}

    @Test
	public void Should_Succeed_When_AnnouncsIsNull() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {
        // should succeed even though the user didn't refer any other announcements when posting
        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(2));
        assertEquals("public post2 from user2", general_result[1]);
    }

	@Test
	public void Should_Succeed_When_Asking_For_All() throws InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, NonceTimeoutException, UserNotRegisteredException, FreshnessException {

        String[] general_result = getMessagesFromJSON(clientEndpoint1.readGeneral(0));

        assertEquals(general_result[0], "message with references from user1");
        assertEquals(general_result[1], "public post2 from user2");
		assertEquals(general_result[2], "public post1 from user2");
        assertEquals(general_result[3], "public post2 from user1");
        assertEquals(general_result[4], "public post1 from user1");
	}
    
    @Test
	public void Should_Succeed_When_CheckingReferencedAnnouncementsResultWith1Post() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {

		// get announcements with references -> which is the most recent one (1)       
        JSONObject result1 = clientEndpoint1.readGeneral(1); 

		int ref_id1_from_user1 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result1)[0]);
		int ref_id1_from_user2 = Integer.parseInt(getReferencedAnnouncementsFromJSONResultWith1Post(result1)[1]);

        assertEquals(0, ref_id1_from_user1);
        assertEquals(3, ref_id1_from_user2);
	}
}
