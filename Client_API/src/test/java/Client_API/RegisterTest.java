package Client_API;

import Exceptions.InvalidPublicKeyException;
import Exceptions.UnknownPublicKeyException;
import org.junit.Test;

import Exceptions.AlreadyRegisteredException;

import java.security.KeyStoreException;
import java.security.PublicKey;

import static org.junit.Assert.assertEquals;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {
	
	@Test
	public void SimpleRegisterSuccess() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
	    deleteUsers();
		clientAPI.register(publicKey2, "user2");
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		// This user is registered @BeforeClass
        clientAPI.register(publicKey2, "user1");
	}

	@Test(expected = UnknownPublicKeyException.class)
	public void Should_Fail_When_ServerDoesntHavePubKey() throws KeyStoreException, AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		PublicKey badPub = keyStore.getCertificate("usererror").getPublicKey();
		clientAPI.register(badPub, "usererror");

	}
	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_PubKeyIsNull() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(null, "userERROR");
	}
	@Test(expected = InvalidPublicKeyException.class)
	public void Should_Fail_When_KeyIsSmaller() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		clientAPI.register(generateSmallerKey(), "error");
	}

	@Test
	public void Should_Succeed_With_TripleRegisters() throws AlreadyRegisteredException, UnknownPublicKeyException, InvalidPublicKeyException {
		deleteUsers();
		assertEquals(1, clientAPI.register(publicKey1, "user1"));
		assertEquals(1, clientAPI.register(publicKey2, "user2"));
		assertEquals(1, clientAPI.register(publicKey3, "user3"));
	}

	
}
