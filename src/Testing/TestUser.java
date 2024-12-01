package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Common.User.User;

class TestUser {
	private static User user;
	
	@BeforeAll
	static public void createConstructor() {
		user=new User("user", "user123");
	}
	@Test
	void testgetUserID() {
		assertEquals(1, user.getUserID());
		
	}
	@Test
	void testgetUsername() {
		assertEquals("user", user.getUsername());
	}
	@Test
	void testgetUserPassword() {
		assertEquals("user123", user.getPassword());
	}
	@Test
	void testSetUsername() {
		user.setUsername("randomeName");
		assertNotEquals("user", user.getUsername());
	}
	@Test
	void testSetUserPass() {
		user.setPassword("randomPass123");
		assertNotEquals("user123", user.getPassword());
	}
	@Test
	void testBanUser() {
		boolean ban=true;
		user.setBanned(ban);
		assertTrue(user.isBanned());
	}
}
