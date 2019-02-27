package test.entity;

import org.junit.Test;

public class Tester {
	@Test
	public void test() {
		User user = new User();
		user.setUsername("tomato");
		user.setAge(24);
		user.setId(10000L);
		user.setPassword("ddd");
		user.save();
	}
	
}
