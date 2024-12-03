package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Common.Admin.Admin;
import Common.ChatBox.ChatBox;
import Common.User.User;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.Server.Server;
import ServerApp.StorageManager.StorageManager;

class TestAdmin {
    private Admin admin;
    private MessageHandler messageHandler;
    private AuthenticationSystem authenticationSys;
    private User user;
    private StorageManager storageManager;
    private ConcurrentHashMap<Integer, ChatBox> chatBoxes;
    private ConcurrentHashMap<Integer, User> userDB;
    private Server server;

    @BeforeEach
    public void setUpConstructor() {
        storageManager = new StorageManager();
        chatBoxes = new ConcurrentHashMap<>();
        authenticationSys = new AuthenticationSystem();
        userDB = authenticationSys.getUserDB();
        messageHandler = new MessageHandler(storageManager, chatBoxes, userDB, server);
        admin = new Admin("BobAdmin", "Bob123", messageHandler, authenticationSys);
        authenticationSys.registerUser(admin);
        user = new User("test", "test123");
    }

    @Test
    void testAddUser() {
        User newUser = new User("newUser", "newPass");
        assertTrue(admin.addUser(newUser));
    }

    @Test
    void testDeleteUser() {
    	admin.addUser(user);
        System.out.println("UserIDelete :" + user.getUserID());
        assertTrue(admin.deleteUser(user.getUserID()));
    }

    @Test
    void testResetPasswordforUser() {
    	admin.addUser(user);
        System.out.println("UserIDReset :" + user.getUserID());
        assertTrue(admin.resetUserPassword(user.getUserID(), "newPass"));
    }

    @Test
    void testBanUser() {
    	admin.addUser(user);
        int id=user.getUserID();
        assertTrue(admin.banUser(id));
    }
    @Test
    void testUnBanUser() {
    	admin.addUser(user);
        int id=user.getUserID();
        admin.banUser(id);
        assertTrue(admin.unbanUser(id));
    }
    @Test
    void testToSendSystemMessage() {
    	admin.sendSystemMessage("Send Message");
    }
}