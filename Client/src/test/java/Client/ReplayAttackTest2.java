package Client;

import Exceptions.*;
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
    public static void populate() throws FreshnessException, UnknownPublicKeyException, NonceTimeoutException, IntegrityException, OperationTimeoutException, AlreadyRegisteredException {
        clientEndpoint1.register();
    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {

        setTestFlag(true);

        clientEndpoint1.post("entao vamos la", null);
        //clientEndpoint1.post("esta nao vai receber resposta", null);


    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post_General() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {

        setTestFlag(true);

        clientEndpoint1.postGeneral("entao vamos la", null);

    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Read() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        setTestFlag(true);

        clientEndpoint1.read("user1", 1);

    }

}
