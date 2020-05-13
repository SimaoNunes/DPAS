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

    @Test(expected = OperationTimeoutException.class)
    public void ShouldNotDoAnythingPost() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.changeNservers(2);
        clientEndpoint1.post("message1", null);

    }

    @Test(expected = OperationTimeoutException.class)
    public void ShouldNotDoAnythingRead() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.changeNservers(2);
        clientEndpoint1.read("user1", 1);

    }

    @Test(expected = OperationTimeoutException.class)
    public void ShouldNotDoAnythingPostGeneral() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.changeNservers(2);
        clientEndpoint1.postGeneral("message1", null);

    }

    @Test(expected = OperationTimeoutException.class)
    public void ShouldNotDoAnythingReadGeneral() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.changeNservers(2);
        clientEndpoint1.readGeneral(1);

    }


}
