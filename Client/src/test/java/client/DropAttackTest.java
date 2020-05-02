package client;

import Exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropAttackTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.post("USER1 MESSAGE1", null);
        clientEndpoint1.postGeneral("USER1 MESSAGE2", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_Post() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
        setDropNonceFlag(true);
        clientEndpoint1.post("vai ser droppada", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_PostGeneral() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.postGeneral("message1", null);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_the_Server_Nonce_Response_Is_Dropped_Register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.register();
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_the_Server_Response_Is_Dropped_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.post("user1", null);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_the_Server_Response_Is_Dropped_Read() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_the_Server_Response_Is_Dropped_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.postGeneral("message a toa", null);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_the_Server_Response_Is_Dropped_ReadGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.readGeneral(1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_the_Server_Response_Is_Dropped_Register() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint2.register();
    }

}
