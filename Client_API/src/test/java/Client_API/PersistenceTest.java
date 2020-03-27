package Client_API;

import Exceptions.*;
import Library.Request;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Scanner;

import static junit.framework.TestCase.assertEquals;

public class PersistenceTest extends BaseTest{

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
        clientAPI.register(publicKey1, "Miguel");
        clientAPI.register(publicKey2, "Grilo");
        clientAPI.register(publicKey3, "Simao");
    }

    @Test(expected = AlreadyRegisteredException.class)
    public void PersistenceRegisterTest() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException, InterruptedException {


        System.out.println("You have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.register(publicKey1, "Miguel");


    }
    @Test
    public void PersistencePostTest() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException {

        clientAPI.post(publicKey1, "message1 user1", null);

        System.out.println("You have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.post(publicKey1, "message2 user1", null);

        String[] result = getMessagesFromJSON(clientAPI.read(publicKey1, 2));

        assertEquals(result[0], "message2 user1");

        assertEquals(result[1], "message1 user1");

    }

    @Test
    public void PersistencePostGeneralTest() throws InvalidAnnouncementException, UserNotRegisteredException, InvalidPublicKeyException, MessageTooBigException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException {

        clientAPI.postGeneral(publicKey2, "general1 user2", null);

        clientAPI.postGeneral(publicKey2, "general2 user2", null);

        System.out.println("You have 7 seconds to reboot the server");
        Thread.sleep(7000);

        clientAPI.postGeneral(publicKey3, "general1 user3", null);

        String[] result = getMessagesFromJSON(clientAPI.readGeneral(2));

        assertEquals(result[0], "general1 user3");

        assertEquals(result[1], "general2 user2");

    }



}
