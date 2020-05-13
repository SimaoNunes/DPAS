package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReplayResponseAttackTest extends BaseTest {

    /**
     * Replay attacks on responses from the server to the operation
     *
     * If the client is not expecting any response from the server, then the replay attack will
     * have no meaning. Although if a user is expecting a response from the server but receives
     * an older response, a replayed message from the past, should ignore it and should ask the
     * user to repeat it.
     * @throws InvalidAnnouncementException 
     * @throws MessageTooBigException 
     * @throws UserNotRegisteredException 
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
    		FreshnessException, IntegrityException, UserNotRegisteredException, MessageTooBigException, InvalidAnnouncementException {
        clientEndpoint1.register();
        clientEndpoint1.post("user1 announc message1", null);
        setReplayFlag(true,1);
    }
   
    @AfterClass
    public static void setReplayFlagFalse() {
    	setReplayFlag(false,1);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.post("user1 announc message2", null);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post_General() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException{
        clientEndpoint1.postGeneral("user1 general message1", null);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.read("user1", 1);
    }

}
