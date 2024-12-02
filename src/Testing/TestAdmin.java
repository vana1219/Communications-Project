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
        userDB = new ConcurrentHashMap<>();
        server = new Server();
		authenticationSys =new AuthenticationSystem();
		messageHandler=new MessageHandler(storageManager, chatBoxes, userDB, server);
		admin =new Admin("Bob", "Bob123", messageHandler, authenticationSys);
		authenticationSys.registerUser(admin);
	}
	
	@Test
	void testAddUser() {		
		user=new User("testUser", "user123");
		assertTrue(admin.addUser(user));
	}
	
	@Test
	void testDeleteUser() {	
		System.out.println("Try Dlete");
		System.out.println(userDB.size());
		assertTrue(admin.deleteUser(1));
	
	}

}

