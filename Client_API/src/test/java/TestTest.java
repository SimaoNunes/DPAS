import static org.junit.Assert.assertEquals;

import org.junit.*;

public class TestTest extends BaseTest{
	
	@Test
	public void Should_Return1_When_InputIs1() {		
		assertEquals(1, clientAPI.always1(1));
	}
	
	@Test
	public void Should_Return1_When_InputIs0() {		
		assertEquals(1, clientAPI.always1(0));
	}

}
