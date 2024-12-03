package Testing;

import static org.junit.jupiter.api.Assertions.*;

import Common.User.User;
import ServerApp.AuthenticationSystem.AuthenticationSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestAuthenticationSystem {
    private AuthenticationSystem authSystem;

    @BeforeEach
    void startAuthentication() {
        authSystem = new AuthenticationSystem();
    }

    @Test
    void testRegisterUser() {
        User user = new User("testUsername", "testPassword");
        assertTrue(authSystem.registerUser(user));
        assertNotNull(authSystem.findUser(user.getUserID()));
    }

    @Test
    void testValidateCredentials() {
        User user = new User("testUsername10", "testPassword10");
        authSystem.registerUser(user);
        assertEquals(user, authSystem.validateCredentials("testUsername10", "testPassword10"));
        assertNull(authSystem.validateCredentials("testUsername10", "wrongPassword"));
    }

    @Test
    void testBanAndUnbanUser() {
        User user = new User("testUsername20", "testPassword20");
        authSystem.registerUser(user);
        assertTrue(authSystem.banUser(user.getUserID()));
        assertTrue(user.isBanned());
        assertTrue(authSystem.unbanUser(user.getUserID()));
        assertFalse(user.isBanned());
    }

    @Test
    void testResetPassword() {
        User user = new User("testUsername30", "testPassword30");
        authSystem.registerUser(user);
        assertTrue(authSystem.resetPassword(user.getUserID(), "newPassword"));
        assertEquals("newPassword", authSystem.findUser(user.getUserID()).getPassword());
    }
}
