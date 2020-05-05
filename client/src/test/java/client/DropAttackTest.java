package client;

import exceptions.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class DropAttackTest extends BaseTest {

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, UserNotRegisteredException, InvalidAnnouncementException, MessageTooBigException {
        clientEndpoint1.register();
        clientEndpoint1.write("USER1 ANNOUNC MESSAGE1", null, false);
        clientEndpoint1.write("USER1 GENERAL MESSAGE1", null, true);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_write() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
        setDropNonceFlag(true);
        clientEndpoint1.write("USER1 ANNOUNC MESSAGE2", null, false);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_Read() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_writeGeneral() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException,
    		NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.write("USER1 GENERAL MESSAGE2", null, true);
    }

    @Test(expected = NonceTimeoutException.class)
    public void When_ServerNonceIsDropped_Register() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
    		OperationTimeoutException, FreshnessException, IntegrityException {
    	setDropOperationFlag(false);
    	setDropNonceFlag(true);
        clientEndpoint1.register();
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_write() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.write("USER1 ANNOUNC MESSAGE3", null, false);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_Read() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.read("user1", 1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_writeGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.write("USER1 GENERAL MESSAGE3", null, true);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_ReadGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint1.readGeneral(1);
    }

    @Test(expected = OperationTimeoutException.class)
    public void When_ServerResponseIsDropped_Register() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, InterruptedException, UnknownPublicKeyException, AlreadyRegisteredException {
    	setDropOperationFlag(true);
    	setDropNonceFlag(false);
        clientEndpoint2.register();
    }

}
