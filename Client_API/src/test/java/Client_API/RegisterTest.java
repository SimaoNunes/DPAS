package Client_API;

import org.junit.Test;

import Exceptions.AlreadyRegisteredException;

////////////////////////////////////////////////////////////////////
//																  //
//   WARNING: Server must be running in order to run these tests  //
//                                                                //
////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////
//                                                     //
//     Only 1 user is already registered (user1)       //
//                                                     //
/////////////////////////////////////////////////////////

public class RegisterTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsValidArray() throws AlreadyRegisteredException {
	        clientAPI.register(publicKey2, "user2");
	}
	
	@Test(expected = AlreadyRegisteredException.class)
	public void Should_Fail_When_UserIsAlreadyRegistered() throws AlreadyRegisteredException {
		// This user is registered @BeforeClass
        clientAPI.register(publicKey1, "user1");
	}

	@Test(expected = )
	public void Should_Fail_When_Server_DoesntHave_PubKey() {

	}
	
}
