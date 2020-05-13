package client;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import exceptions.AlreadyRegisteredException;
import exceptions.FreshnessException;
import exceptions.IntegrityException;
import exceptions.InvalidAnnouncementException;
import exceptions.InvalidPostsNumberException;
import exceptions.MessageTooBigException;
import exceptions.NonceTimeoutException;
import exceptions.OperationTimeoutException;
import exceptions.TooMuchAnnouncementsException;
import exceptions.UnknownPublicKeyException;
import exceptions.UserNotRegisteredException;

public class IntegrityRequestTolerateFaultTest extends BaseTest {

	/**
	 *
	 * Integrity test of the request operation from the client to the server
	 */
	
	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
			FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
		clientEndpoint1.register();
		clientEndpoint2.register();
		clientEndpoint3.register();
		clientEndpoint1.post("user1 announc message1", null);
		clientEndpoint2.post("user2 announc message1", null);
		clientEndpoint1.postGeneral("user1 general message1", null);
		clientEndpoint2.postGeneral("user2 general message1", null);
		clientEndpoint3.post("user3 announc message1", null);
		clientEndpoint3.post("user3 announc message2", null);
		clientEndpoint1.setIntegrityFlag(true);
	}
	
	@AfterClass
	public static void turnFlagOff() {
		clientEndpoint1.setIntegrityFlag(false);
	}

	@Test
	public void Should_Tolerate_When_PostIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send to 1 server the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		assertEquals(1, clientEndpoint1.post("Olá, gosto muito de ti", null));
	}
	
	@Test
	public void Should_Tolerate_When_PostGeneralIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send to 1 server the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		assertEquals(1, clientEndpoint1.postGeneral("Olá, gosto muito de ti", null));
	}

	@Test
	public void Should_Tolerate_When_ReadIsTamperedWith() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
			NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, MessageTooBigException, InvalidAnnouncementException {
		// Flag will make endpoint send to 1 server that userX wants to read from user3
		clientEndpoint1.post("user1 announc message2", null);
    	String[] result = getMessagesFromJSON(clientEndpoint1.read("user1", 2));
    	assertEquals("user1 announc message2", result[0]);
	}
	
	@Test
	public void Should_Tolerate_When_ReadGeneralIsTamperedWith() throws UserNotRegisteredException, InvalidPostsNumberException, TooMuchAnnouncementsException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException, MessageTooBigException, InvalidAnnouncementException {
		// Flag will make endpoint send to 1 server that userX wants to read from user3
		clientEndpoint1.postGeneral("user1 general message2", null);
    	String[] result = getMessagesFromJSONGeneral(clientEndpoint1.readGeneral(2));
    	assertEquals("user1 general message2", result[0]);
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Throw_When_RegisterIsTamperedWith() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send to 1 server a register with the pubKey from user3
		assertEquals(1, clientEndpoint1.register());
	}
	
}
