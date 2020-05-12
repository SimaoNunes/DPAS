package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//
//	WARNING: Server must be running in order to run these tests
//
////////////////////////////////////////////////////////////////////

/* We are testing concurrent read of a register while writing to it. You are assuming the sleep we apply
 * 	for the read to be concurrent is enough for the value to be updated on all correct servers, therefore
 * 	we are expecting the output to be the updated value.
 */

public class AtomicWriterTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        clientEndpoint3.register();
        setAtomicWriteFlag(true);        
    }
    
    @AfterClass
    public static void turnOffFaggot() {
    	setAtomicWriteFlag(false);
    }

    @Test
    public void Should_Succeed_When_1ConcurrentReader() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {
    	
    	clientEndpoint1.post("First test message user1", null);
    	
        String[][] result = new String[1][1];

        Thread threadRead = new Thread(new Runnable() {
            public void run() {
                try {
                    result[0] = getMessagesFromJSON(clientEndpoint2.read("user1", 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadRead.start();
        
        clientEndpoint1.post("This should be the one returned user1", null);

        while(threadRead.isAlive()) {
        }

        assertEquals("This should be the one returned user1", result[0][0]);
    }
    
    @Test
    public void Should_Succeed_When_2ConcurrentReaders() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {
    	
    	clientEndpoint1.post("First test message user1", null);
    	
        String[][] results = new String[2][1];

        Thread threadReader2 = new Thread(new Runnable() {
            public void run() {
                try {
                    results[0] = getMessagesFromJSON(clientEndpoint2.read("user1", 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadReader2.start();
        
        Thread threadReader3 = new Thread(new Runnable() {
            public void run() {
                try {
                    results[1] = getMessagesFromJSON(clientEndpoint3.read("user1", 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        threadReader3.start();
        
        clientEndpoint1.post("This should be the one returned user1", null);

        while(threadReader2.isAlive() || threadReader3.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertEquals("This should be the one returned user1", results[0][0]);
        assertEquals("This should be the one returned user1", results[1][0]);
    }
    
}
