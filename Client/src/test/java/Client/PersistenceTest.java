package Client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PersistenceTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
        clientEndpoint1.register();
    }

    @Test(expected = AlreadyRegisteredException.class)
    public void PersistenceRegisterTest() throws InterruptedException, AlreadyRegisteredException, UnknownPublicKeyException, IntegrityException, OperationTimeoutException, FreshnessException, NonceTimeoutException {

        System.out.println("You have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientEndpoint1.register();
    }
    
    @Test
    public void PersistencePostTest() throws MessageTooBigException, UserNotRegisteredException,
                                                     InvalidAnnouncementException, InterruptedException, InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, FreshnessException, NonceTimeoutException {

        clientEndpoint1.post( "message1 user1", null);

        System.out.println("\nYou have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientEndpoint1.post("message2 user1", null);

        String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2));

        assertEquals(result[0], "message2 user1");
        assertEquals(result[1], "message1 user1");
    }

    @Test
    public void PersistencePostGeneralTest() throws MessageTooBigException, UserNotRegisteredException,
                                                            InvalidAnnouncementException, InterruptedException, InvalidPostsNumberException, TooMuchAnnouncementsException, IntegrityException, OperationTimeoutException, FreshnessException, NonceTimeoutException, UnknownPublicKeyException, AlreadyRegisteredException {

        clientEndpoint1.postGeneral( "general1 user2", null);
        clientEndpoint2.register();

        clientEndpoint2.postGeneral("general2 user2", null);

        System.out.println("\nYou have 7 seconds to reboot the server");
        Thread.sleep(7000);


        clientEndpoint3.register();
        clientEndpoint3.postGeneral("general1 user3", null);

        String[] result = getMessagesFromJSON(clientEndpoint3.readGeneral(2));

        assertEquals(result[0], "general1 user3");
        assertEquals(result[1], "general2 user2");
    }



}
