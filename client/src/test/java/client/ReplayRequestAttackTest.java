package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ReplayRequestAttackTest extends BaseTest {
	
    /**
     * Replay attacks on the request of the operation by the client
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
    		FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
        clientEndpoint1.register();
        clientEndpoint1.post("user1 announc message1", null);
        clientEndpoint1.postGeneral("user1 general message1", null);
        clientEndpoint1.setReplayFlag(true);
    }
    
    @AfterClass
    public static void setReplayFlagFalse() {
    	clientEndpoint1.setReplayFlag(false);
    }

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException,
    		IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        clientEndpoint1.post("user1 announc message2", null);
        clientEndpoint1.read("user1", 3); //se o replay for bem sucedido, ha 3 posts no server
    }

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One_General() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException,
    		IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        clientEndpoint1.postGeneral("user1 general message2", null);
        clientEndpoint1.readGeneral(3); // se o replay for bem sucedido, ha 3 posts no server
    }


}