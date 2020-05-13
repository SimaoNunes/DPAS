package client;

import exceptions.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntegrityResponseTolerateFaultTest extends BaseTest{

    /**
     * Integrity test of the response from the server to the user's request
     */

    @BeforeClass
    public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
                                                 FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
        clientEndpoint1.register();
        clientEndpoint2.register();
        
        clientEndpoint1.post("message from user1", null);
        clientEndpoint2.post("message from user2", null);
        clientEndpoint2.postGeneral("message general just to populate", null);
        
        setIntegrityFlag(true,1);
    }

    @After
    public void slepp() throws InterruptedException {
        Thread.sleep(5000);
    }

    @AfterClass
    public static void turnFlagOff() {
        setIntegrityFlag(false,1);

    }

    @After
    public void waitMethod() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void Should_Tolerate_Post() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException {
        
        assertEquals(1, clientEndpoint1.post("user1 announc message1", null));

    }

    @Test
    public void Should_Tolerate_Read() throws NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException,
            MessageTooBigException, InvalidAnnouncementException {

        clientEndpoint1.post("message2 from user1", null);
    	String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 1));
    	assertEquals("message2 from user1", result[0]);

    }

    @Test
    public void Should_Tolerate_PostGeneral() throws MessageTooBigException, InvalidAnnouncementException, NonceTimeoutException, FreshnessException, UserNotRegisteredException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException {

        assertEquals(1, clientEndpoint1.postGeneral("user1 announc general message1", null));

    }

    @Test
    public void Should_Tolerate_ReadGeneral() throws IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, NonceTimeoutException, UserNotRegisteredException, FreshnessException,
            MessageTooBigException, InvalidAnnouncementException {

        clientEndpoint1.postGeneral("user1 announc general message2", null);
    	String[] result = getMessagesFromJSONGeneral(clientEndpoint1.readGeneral(1));
    	assertEquals("user1 announc general message2", result[0]);

    }

    @Test(expected = AlreadyRegisteredException.class)
    public void Should_throw_AlreadyRegisteredException_Register() throws NonceTimeoutException, FreshnessException, IntegrityException, OperationTimeoutException, TooMuchAnnouncementsException, InvalidPostsNumberException, UnknownPublicKeyException, AlreadyRegisteredException {
        clientEndpoint1.register();
    }

    @Test(expected = TooMuchAnnouncementsException.class)
	public void Should_Fail_When_AskingAlotOfPosts() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.read("user1", 50);
    }
    
    @Test(expected = InvalidPostsNumberException.class)
	public void Should_Fail_When_BadPostsNumber() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.read("user1", -10);
    }

    @Test(expected = InvalidAnnouncementException.class)
	public void Should_Fail_When_AnnouncDoesntExist() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		int[] announcs1 = {20};
		assertEquals(1, clientEndpoint1.post("user1 referenced announcements:", announcs1));
    }
    
    @Test(expected = MessageTooBigException.class)
	public void Should_Fail_When_MessageIsTooBig() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.post("Has 256 charssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss" +
					   "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss", null);
    }

    @Test(expected = UserNotRegisteredException.class)
	public void Should_Fail_When_UserIsNotRegistered() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint3.post("I am not a registered user", null);
	}


}
