package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class ByzantineClient extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        clientEndpoint3.register();
    }

    @Test
    public void ShouldNotDoAnything() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.changeNservers(2);
        //clientEndpoint1.post("will not be posted", null);
        clientEndpoint1.read("user1", 1);

    }
}
