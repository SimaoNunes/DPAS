package client;

import org.junit.After;
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

public class IntegrityRequestTest extends BaseTest {

	/**
	 *
	 * Integrity test of the request operation from the client to the server
	 */
	
	@BeforeClass
	public static void populate() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException,
			FreshnessException, IntegrityException, MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException {
		clientEndpoint1.register();
		clientEndpoint2.register();
		clientEndpoint1.write("message from user1", null, false);
		clientEndpoint2.write("message from user2", null, false);
	}
	
	@After
	public void turnFlagOff() {
		clientEndpoint1.setIntegrityFlag(false);
	}

	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_PostIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		clientEndpoint1.setIntegrityFlag(true);
		clientEndpoint1.write("Olá, gosto muito de ti", null, false);
	}
	
	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_PostGeneralIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		clientEndpoint1.setIntegrityFlag(true);
		clientEndpoint1.write("Olá, gosto muito de ti", null, true);
	}

	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_ReadIsTamperedWith() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
			NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send that userX wants to read from user3
		clientEndpoint1.setIntegrityFlag(true);
		clientEndpoint1.read("user2", 1);
	}
	
	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_RegisterIsTamperedWith() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send a register with the pubKey from user3
		clientEndpoint1.setIntegrityFlag(true);
		clientEndpoint1.register();
	}
	
}
