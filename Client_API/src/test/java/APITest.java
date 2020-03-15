import static org.junit.Assert.assertEquals;

import org.junit.Test;

import Client_API.ClientAPI;

public class APITest {

	@Test
	public void always1ShouldReturn1() {
		ClientAPI tester = new ClientAPI();
		
		assertEquals(1, tester.always1(2));
		assertEquals(1, tester.always1(0));
	}
	
}
