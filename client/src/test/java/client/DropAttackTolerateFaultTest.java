package client;

import exceptions.*;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropAttackTolerateFaultTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.post("user1 announc message1", null);
        clientEndpoint1.postGeneral("user1 general message1", null);
    }
    
    @AfterClass
    public static void turnOffFlag() {
		setDropNonceFlag(false);
		setDropOperationFlag(false);
    }

    @Test
    public void When_ServerNonceIsDropped_Post() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
        setDropNonceFlag(true);
        assertEquals(1, clientEndpoint1.post("user1 announc message2", null));
    }

    @Test
    public void When_ServerNonceIsDropped_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, MessageTooBigException, InvalidAnnouncementException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
    	clientEndpoint1.post("user1 announc message3", null);
    	String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2));
    	assertEquals("user1 announc message3", result[0]);
    }

    @Test
    public void When_ServerNonceIsDropped_PostGeneral() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        assertEquals(1, clientEndpoint1.postGeneral("user1 general message2", null));
    }

    @Test
    public void When_ServerNonceIsDropped_Register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        assertEquals(1, clientEndpoint2.register());
    }

    @Test
    public void When_ServerResponseIsDropped_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        assertEquals(1, clientEndpoint1.post("user1 announc message4", null));
    }

    @Test
    public void When_ServerResponseIsDropped_Read() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
    	clientEndpoint1.post("user1 announc message5", null);
    	String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2));
    	assertEquals("user1 announc message5", result[0]);
    }

    @Test
    public void When_ServerResponseIsDropped_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        assertEquals(1, clientEndpoint1.postGeneral("user1 general message3", null));
    }

    public void When_ServerResponseIsDropped_ReadGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
    	clientEndpoint1.postGeneral("user1 general message4", null);
    	String[] result = getMessagesFromJSON(clientEndpoint1.readGeneral(2));
    	assertEquals("user1 general message4", result[0]);
    }

    @Test
    public void When_ServerResponseIsDropped_Register() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        assertEquals(1, clientEndpoint3.register());
    }

}
