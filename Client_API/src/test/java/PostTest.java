import static org.junit.Assert.assertEquals;

import org.junit.*;

public class PostTest extends BaseTest {
	
	@Test
	public void Should_Succeed_When_AnnouncsIsNull() {		
		assertEquals(1, clientAPI.post(publicKey, "teste", null));
	}

}
