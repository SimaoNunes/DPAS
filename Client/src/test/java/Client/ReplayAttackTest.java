package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ReplayAttackTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.post("USER1 MESSAGE1", null);

        //clientEndpoint1.postGeneral("USER1 MESSAGE3", null);
    }

    /**
     * Replay attacks on the response of the operation of the server
     */

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        clientEndpoint1.setReplay_flag(true);

        clientEndpoint1.post("this message will not be repeated", null);

        clientEndpoint1.read("user1", 3); //se o replay for bem sucedido, ha 3 posts no server
    }

    @Test(expected = TooMuchAnnouncementsException.class)
    public void Should_not_Post_More_Than_One_General() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {
        clientEndpoint1.setReplay_flag(true);

        clientEndpoint1.postGeneral("this message will not be repeated", null);

        clientEndpoint1.readGeneral(4); //se o replay for bem sucedido, ha 3 posts no server
    }

    @Test
    public void Should_not_Read_More_Than_One() throws UserNotRegisteredException, NonceTimeoutException, OperationTimeoutException, InvalidPostsNumberException, IntegrityException, TooMuchAnnouncementsException, FreshnessException {
        clientEndpoint1.setReplay_flag(true);

        clientEndpoint1.read("user1", 2);
        String[] result1 = getMessagesFromJSON(clientEndpoint1.read("user1", 2));

        assertEquals(result1[0], "this message will not be repeated");
        assertEquals(result1[1], "USER1 MESSAGE1");
        assertEquals(result1.length, 2);
    }

    @Test
    public void Should_not_Read_More_Than_One_General() throws UserNotRegisteredException, NonceTimeoutException, OperationTimeoutException, InvalidPostsNumberException, IntegrityException, TooMuchAnnouncementsException, FreshnessException, MessageTooBigException, InvalidAnnouncementException {

        clientEndpoint1.postGeneral("USER1 MESSAGE2", null);

        clientEndpoint1.setReplay_flag(true);

        clientEndpoint1.postGeneral("USER1 MESSAGE3", null);


        String[] result1 = getMessagesFromJSON(clientEndpoint1.readGeneral(2));

        assertEquals(result1[0], "USER1 MESSAGE3");
        assertEquals(result1[1], "USER1 MESSAGE2");
        assertEquals(result1.length, 2);
    }

    @Test
    public void Should_not_Register_More_Than_One_Time() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException {
        clientEndpoint1.setReplay_flag(true);

        clientEndpoint2.register();

    }

    /**
     * Replay attacks on responses from the server to the operation
     *
     * If the client is not expecting any response from the server, then the replay attack will
     * have no meaning. Although if a user is expecting a response from the server but receives
     * an older response, a replayed message from the past, should ignore it and should ask the
     * user to repeat it.
     */

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.setLater_timeout(false);
        clientEndpoint1.setNonce_flag(false);
        clientEndpoint1.setOperation_flag(false);
        clientEndpoint1.setTest_flag(false);

        setTestFlag(true);

        clientEndpoint1.post("entao vamos la", null);
        clientEndpoint1.post("esta nao vai receber resposta", null);

    }

    @Test(expected = FreshnessException.class)
    public void Should_Ignore_Older_Replayed_Message_Post_General() throws NonceTimeoutException, OperationTimeoutException, IntegrityException, FreshnessException, UnknownPublicKeyException, AlreadyRegisteredException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.setLater_timeout(false);
        clientEndpoint1.setNonce_flag(false);
        clientEndpoint1.setOperation_flag(false);
        clientEndpoint1.setTest_flag(false);

        setTestFlag(true);

        clientEndpoint1.postGeneral("entao vamos la", null);
        clientEndpoint1.postGeneral("esta nao vai receber resposta", null);

    }











}
