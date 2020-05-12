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
        clientEndpoint3.setWaitForReadCompleteFlag(true);
        setGeneralConcurrentWriteFlag(true);
    }

    @AfterClass
    public static void turnOffFaggot() {
        clientEndpoint3.setWaitForReadCompleteFlag(false);
        setGeneralConcurrentWriteFlag(false);
    }

    @Test
    public void Should_Succeed_When_2ConcurrentWriter1Reader() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {

        String[][] result = new String[2][1];

        Thread threadRead = new Thread(new Runnable() {
            public void run() {
                try {
                    clientEndpoint1.post(" message1 user1", null);
                } catch (Exception e) {
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
        setGeneralConcurrentWriteFlag(false);

        clientEndpoint2.post("This should be the one returned user1", null);

        while(threadRead.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals("This should be the one returned user1", result[0][0]);
    }
}
