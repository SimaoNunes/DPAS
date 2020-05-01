package Client;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import Exceptions.AlreadyRegisteredException;
import Exceptions.FreshnessException;
import Exceptions.IntegrityException;
import Exceptions.InvalidAnnouncementException;
import Exceptions.InvalidPostsNumberException;
import Exceptions.MessageTooBigException;
import Exceptions.NonceTimeoutException;
import Exceptions.OperationTimeoutException;
import Exceptions.TooMuchAnnouncementsException;
import Exceptions.UnknownPublicKeyException;
import Exceptions.UserNotRegisteredException;

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
		clientEndpoint1.post("message from user1", null);
		clientEndpoint2.post("message from user2", null);
	}
	
	@After
	public void turnFlagOff() {
		clientEndpoint1.setIntegrity_flag(false);
	}

	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_PostIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		clientEndpoint1.setIntegrity_flag(true);
		clientEndpoint1.post("Olá, gosto muito de ti", null);
	}
	
	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_PostGeneralIsTamperedWith() throws MessageTooBigException, UserNotRegisteredException, InvalidAnnouncementException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send the message "Olá, eu odeio-te" but with the old hash. Server must detect this
		clientEndpoint1.setIntegrity_flag(true);
		clientEndpoint1.postGeneral("Olá, gosto muito de ti", null);
	}

	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_ReadIsTamperedWith() throws InvalidPostsNumberException, UserNotRegisteredException, TooMuchAnnouncementsException,
			NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send that userX wants to read from user3
		clientEndpoint1.setIntegrity_flag(true);
		clientEndpoint1.read("user2", 1);
	}
	
	@Test(expected = OperationTimeoutException.class)
	public void Should_Ignore_When_RegisterIsTamperedWith() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException,
			OperationTimeoutException, FreshnessException, IntegrityException {
		// Flag will make endpoint send a register with the pubKey from user3
		clientEndpoint1.setIntegrity_flag(true);
		clientEndpoint1.register();
	}
	
}
