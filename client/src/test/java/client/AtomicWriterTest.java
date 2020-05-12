package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.security.PublicKey;

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
    public void Should_Succed_When_1ConcurrentReader() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {

    	clientEndpoint1.post("First test message user1", null);
    	
        String[][] result = new String[1][1];

        Thread threadRead = new Thread(new Runnable() {
            public void run() {
                try {
                    result[0] = getMessagesFromJSON(clientEndpoint2.read("user1", 1));
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
        
        clientEndpoint1.post("This should be the one returned user1", null);

        while(true) {
        	if(!threadRead.isAlive()) {
        		break;
        	}
        }

        assertEquals("This should be the one returned user1", result[0][0]);
    }
    
    @Test
    public void Should_Succed_When_2ConcurrentReaders() throws UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, MessageTooBigException, FreshnessException, IntegrityException, OperationTimeoutException {

    	clientEndpoint1.post("First test message user1", null);
    	
        String[][] results = new String[2][1];

        Thread threadReader2 = new Thread(new Runnable() {
            public void run() {
                try {
                    results[0] = getMessagesFromJSON(clientEndpoint2.read("user1", 1));
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
        threadReader2.start();
        
        Thread threadReader3 = new Thread(new Runnable() {
            public void run() {
                try {
                    results[1] = getMessagesFromJSON(clientEndpoint3.read("user1", 1));
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
        threadReader3.start();
        
        clientEndpoint1.post("This should be the one returned user1", null);

        while(true) {
        	if(!threadReader2.isAlive() && !threadReader3.isAlive()) {
        		break;
        	}
        }

        assertEquals("This should be the one returned user1", results[0][0]);
        assertEquals("This should be the one returned user1", results[1][0]);
    }
    
}
