package client;

import exceptions.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneralConcurrentWritersTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        clientEndpoint3.register();
    }

    @AfterClass
    public static void turnOffFaggot() {
        setGeneralConcurrentWriteFlag(false);
    }

    @Test
    public void Should_Succeed_When_2ConcurrentWriter1ReaderStaysTheLast() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        setGeneralConcurrentWriteFlag(true);

        String[] results;

        Thread threadWrite = new Thread(new Runnable() {
            public void run() {
                try {
                    clientEndpoint1.postGeneral(" message1 user1", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadWrite.start();

        setGeneralConcurrentWriteFlag(false);

        Thread threadWrite2 = new Thread(new Runnable() {
            public void run() {
                try {
                    clientEndpoint2.postGeneral("This should be the one returned user2", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadWrite2.start();

        while(threadWrite.isAlive() || threadWrite2.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        results = getMessagesFromJSONGeneral(clientEndpoint2.readGeneral(1));
        assertEquals("This should be the one returned user2", results[0]);
    }

    @Test
    public void Should_Succeed_When_2ConcurrentWriter1ReaderStaysTheFirst() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        setGeneralConcurrentWriteFlag(true);

        String[] results;

        Thread threadWrite = new Thread(new Runnable() {
            public void run() {
                try {
                    clientEndpoint2.postGeneral("This should be the one returned user2 test2", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadWrite.start();

        setGeneralConcurrentWriteFlag(false);

        Thread threadWrite2 = new Thread(new Runnable() {
            public void run() {
                try {
                    clientEndpoint1.postGeneral("message user1 will be shifted test2", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadWrite2.start();

        while(threadWrite.isAlive() || threadWrite2.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        results = getMessagesFromJSONGeneral(clientEndpoint2.readGeneral(1));
        assertEquals("This should be the one returned user2 test2", results[0]);
    }
}
