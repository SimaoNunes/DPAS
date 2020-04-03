package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class ReplayAttackTest1 extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.post("USER1 MESSAGE1", null);

        //clientEndpoint1.postGeneral("USER1 MESSAGE3", null);
    }

    /**
     * Replay attacks on the request of the operation by the client
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



}
