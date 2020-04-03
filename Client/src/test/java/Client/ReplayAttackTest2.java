package Client;

import Exceptions.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReplayAttackTest2 extends BaseTest {

    /**
     * Replay attacks on responses from the server to the operation
     *
     * If the client is not expecting any response from the server, then the replay attack will
     * have no meaning. Although if a user is expecting a response from the server but receives
     * an older response, a replayed message from the past, should ignore it and should ask the
     * user to repeat it.
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
    		FreshnessException, IntegrityException {
        clientEndpoint1.register();
    }
    
    @Before
    public void setTestFlagTrue() {
    	setTestFlag(true);
    }
    
    @AfterClass
    public void setTestFlagFalse() {
    	setTestFlag(true);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.post("entao vamos la", null);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post_General() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException{
        clientEndpoint1.postGeneral("entao vamos la", null);
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.read("user1", 1);
    }

}
