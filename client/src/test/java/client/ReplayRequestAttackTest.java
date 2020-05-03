package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class ReplayRequestAttackTest extends BaseTest {
	
    /**
     * Replay attacks on the request of the operation by the client
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
    		FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
        clientEndpoint1.register();
        clientEndpoint1.post("USER1 ANNOUNC MESSAGE1", null);
        clientEndpoint1.postGeneral("USER1 GENERAL MESSAGE1", null);
    }
    
    @Before
    public void setReplayFlagTrue() {
    	clientEndpoint1.setReplayFlag(true);
    }
    
    @AfterClass
    public static void setReplayFlagFalse() {
    	clientEndpoint1.setReplayFlag(false);
    }

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException,
    		IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.post("USER1 ANNOUNC MESSAGE2", null);
        clientEndpoint1.read("user1", 3); //se o replay for bem sucedido, ha 3 posts no server
    }

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One_General() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException,
    		IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.postGeneral("USER1 GENERAL MESSAGE2", null);
        clientEndpoint1.readGeneral(3); //se o replay for bem sucedido, ha 3 posts no server
    }

    @Test
    public void Should_not_Read_More_Than_One() throws UserNotRegisteredException, NonceTimeoutException, OperationTimeoutException,
    		InvalidPostsNumberException, IntegrityException, TooMuchAnnouncementsException, FreshnessException {
        clientEndpoint1.read("user1", 2);
        
        String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 2));

        assertEquals(result1[0], "USER1 GENERAL MESSAGE2");
        assertEquals(result1[1], "USER1 MESSAGE1");
        assertEquals(result1.length, 2);
    }

    @Test
    public void Should_not_Read_More_Than_One_General() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, InvalidPostsNumberException, TooMuchAnnouncementsException {
        clientEndpoint1.postGeneral("USER1 MESSAGE3", null);

        String[] result1 = getMessagesFromJSON(clientEndpoint1.readGeneral(1));

        assertEquals(result1[0], "USER1 MESSAGE3");
        assertEquals(result1.length, 1);
    }

    @Test
    public void Should_not_Register_More_Than_One_Time() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException,
    		AlreadyRegisteredException {
        clientEndpoint2.setReplayFlag(true);
        assertEquals(1, clientEndpoint2.register());
    }

}