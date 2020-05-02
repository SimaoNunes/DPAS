package client;

import Exceptions.*;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyStoreException;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {
	
	@Before
	public void callDeleteUsers() {
		deleteUsers();
	}
	
	@Test
	public void SimpleRegisterSuccess() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.register();
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpoint1.register();
		clientEndpoint1.register();
	}

	@Test(expected = UnknownPublicKeyException.class)
	public void Should_Fail_When_ServerDoesntHavePubKey() throws KeyStoreException, AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		clientEndpointError.register();
	}
	
	/*@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_PubKeyIsNull() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		// ERA FIXE TESTAR PARA CHAVE A NULL
		clientAPI.register(null, "userERROR", privateKey1);
	}*/

	@Test
	public void Should_Succeed_With_TripleRegisters() throws AlreadyRegisteredException, UnknownPublicKeyException, NonceTimeoutException, OperationTimeoutException, FreshnessException, IntegrityException {
		assertEquals(1, clientEndpoint1.register());
		assertEquals(1, clientEndpoint2.register());
		assertEquals(1, clientEndpoint3.register());
	}
	
}
