package client;

import exceptions.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropAttackTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.post("user1 announc message1", null);
        clientEndpoint1.postGeneral("user1 general message1", null);
    }
    
    @AfterClass
    public static void turnOffFlag() {
		setDropNonceFlag(false,4);
		setDropOperationFlag(false,4);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_Post() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false,4);
        setDropNonceFlag(true,4);
        clientEndpoint1.post("user1 announc message2", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false,4);
    	setDropNonceFlag(true,4);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_PostGeneral() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false,4);
    	setDropNonceFlag(true,4);
        clientEndpoint1.postGeneral("user1 general message2", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_Register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false,4);
    	setDropNonceFlag(true,4);
        clientEndpoint1.register();
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true,4);
    	setDropNonceFlag(false,4);
        clientEndpoint1.post("user1 announc message3", null);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_Read() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true,4);
    	setDropNonceFlag(false,4);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true,4);
    	setDropNonceFlag(false,4);
        clientEndpoint1.postGeneral("user1 general message3", null);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_ReadGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true,4);
    	setDropNonceFlag(false,4);
        clientEndpoint1.readGeneral(1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_Register() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true,4);
    	setDropNonceFlag(false,4);
        clientEndpoint2.register();
    }

}
