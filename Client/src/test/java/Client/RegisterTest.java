package Client;

import Exceptions.AlreadyRegisteredException;
import Exceptions.InvalidPublicKeyException;
import Exceptions.UnknownPublicKeyException;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyStoreException;
import java.security.PublicKey;

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
	public void SimpleRegisterSuccess() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientEndpoint1.register();
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientEndpoint1.register();
		clientEndpoint1.register();
	}

	/*@Test(expected = UnknownPublicKeyException.class)
	public void Should_Fail_When_ServerDoesntHavePubKey() throws KeyStoreException, AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		PublicKey badPub = keyStore.getCertificate("usererror").getPublicKey();
		clientAPI.register(badPub, "usererror", privateKey1);
	}*/
	
	/*@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_PubKeyIsNull() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(null, "userERROR", privateKey1);
	}*/
	
	/*@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_KeyIsSmaller() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(generateSmallerKey(), "error", privateKey1);
	}*/

	/*@Test
	public void Should_Succeed_With_TripleRegisters() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		assertEquals(1, clientAPI.register(publicKey1, "user1", privateKey1));
		assertEquals(1, clientAPI.register(publicKey2, "user2", privateKey2));
		assertEquals(1, clientAPI.register(publicKey3, "user3", privateKey3));
	}*/

	
}
