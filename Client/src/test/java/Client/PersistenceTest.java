package Client;

import Exceptions.AlreadyRegisteredException;
import Exceptions.UnknownPublicKeyException;
import org.junit.BeforeClass;

public class PersistenceTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException {
        clientEndpoint1.register();
    }

    /*@Test(expected = AlreadyRegisteredException.class)
    public void PersistenceRegisterTest() throws InterruptedException, AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {

        System.out.println("You have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.register(publicKey1, "Miguel");
    }*/
    
    /*@Test
    public void PersistencePostTest() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException,
    		InvalidAnnouncementException, InterruptedException, InvalidPostsNumberException, TooMuchAnnouncementsException {

        clientAPI.post(publicKey1, "message1 user1", null);

        System.out.println("\nYou have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.post(publicKey1, "message2 user1", null);

        String[] result = getMessagesFromJSON(clientAPI.read(publicKey1, 2));

        assertEquals(result[0], "message2 user1");
        assertEquals(result[1], "message1 user1");
    }*/

    /*@Test
    public void PersistencePostGeneralTest() throws MessageTooBigException, UserNotRegisteredException, InvalidPublicKeyException,
    		InvalidAnnouncementException, InterruptedException, InvalidPostsNumberException, TooMuchAnnouncementsException {

        clientAPI.postGeneral(publicKey2, "general1 user2", null);
        clientAPI.postGeneral(publicKey2, "general2 user2", null);

        System.out.println("\nYou have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.postGeneral(publicKey3, "general1 user3", null);

        String[] result = getMessagesFromJSON(clientAPI.readGeneral(2));

        assertEquals(result[0], "general1 user3");
        assertEquals(result[1], "general2 user2");
    }*/



}
