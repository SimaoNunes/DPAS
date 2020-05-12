package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        clientEndpoint1.post("First test message user1", null);

    }

    @Test
    public void ShouldSuccedWhenConcurrentWrite() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {

        setAtomicWriteFlag(true);

        String[][] result = new String[1][1];

        Thread threadRead = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = getMessagesFromJSON(clientEndpoint1.read("user1", 1));
                } catch (UserNotRegisteredException e) {
                    e.printStackTrace();
                } catch (InvalidPostsNumberException e) {
                    e.printStackTrace();
                } catch (TooMuchAnnouncementsException e) {
                    e.printStackTrace();
                } catch (NonceTimeoutException e) {
                    e.printStackTrace();
                } catch (OperationTimeoutException e) {
                    e.printStackTrace();
                } catch (FreshnessException e) {
                    e.printStackTrace();
                } catch (IntegrityException e) {
                    e.printStackTrace();
                }
            }
        });

        threadRead.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        clientEndpoint1.post("This should be the one returned user1", null);

        assertEquals(result[0][0], "This should be the one returned user1");
    }
}
