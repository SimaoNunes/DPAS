package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropAttackTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.register();
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException {
        clientEndpoint1.setTimeout(10);
        clientEndpoint1.post("vai ser droppada", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_Read() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.setTimeout(4000);
        clientEndpoint1.post("message1", null);
        clientEndpoint1.setTimeout(10);
        clientEndpoint1.read("user1", 1);
    }
}
