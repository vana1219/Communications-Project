package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Common.ChatBox.ChatBox;
import Common.Message.Message;
import Common.User.User;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.Server.Server;
import ServerApp.StorageManager.StorageManager;

class TestMessageHandler {
	private MessageHandler msgHandler;
	private StorageManager storageManager; 
	private ChatBox chatbox;
    private ConcurrentHashMap<Integer, ChatBox> chatBoxes; 
    private ConcurrentHashMap<Integer, User> userDB; 
    private Server server; 
    
    @BeforeEach
    public void setUpMessageHandler() {
    	storageManager= new StorageManager();
    	chatbox=new ChatBox();
    	chatBoxes = new ConcurrentHashMap<>();
        userDB = new ConcurrentHashMap<>();
        server=new Server();
    	msgHandler=new MessageHandler(storageManager,chatBoxes,userDB,server);
    }
	@Test
	void testCreateChatBox() {
		User user1=new User("user1", "pass");
		User user2=new User("user2", "pass");
		chatbox.addParticipant(user1);
		chatbox.addParticipant(user2);
		List<User> participants =chatbox.getParticipantsList();
		assertNotNull(msgHandler.createChatBox(participants, "chat1"));
	}
	@Test
	void testGetChatBox() {
		User user1=new User("user1", "pass");
		User user2=new User("user2", "pass");
		chatbox.addParticipant(user1);
		chatbox.addParticipant(user2);
		List<User> participants =chatbox.getParticipantsList();
		msgHandler.createChatBox(participants, "chat1");
		int boxId=chatbox.getChatBoxID();
		assertNotNull(msgHandler.getChatBox(boxId));
		
	}
	@Test
	void testSendMessage() {
		User user1=new User("user1", "pass");
		User user2=new User("user2", "pass");
		chatbox.addParticipant(user1);
		chatbox.addParticipant(user2);
		List<User> participants =chatbox.getParticipantsList();
		msgHandler.createChatBox(participants, "chat1");
		int boxId=chatbox.getChatBoxID();
		Message msg=new Message(1,"This is test msg");
		assertTrue(msgHandler.sendMessage(boxId,msg));
	}
	@Test
	void testSendMessageToUser() {
		User user1=new User("user1", "pass");
		User user2=new User("user2", "pass");
		chatbox.addParticipant(user1);
		chatbox.addParticipant(user2);
		List<User> participants =chatbox.getParticipantsList();
		msgHandler.createChatBox(participants, "chat1");
		int userId=user1.getUserID();
		assertTrue(msgHandler.sendMessageToUser(userId, "Hi There"));
	}
	@Test
	void testRemoveParticipantFromChatBox() {
		User user1=new User("user1", "pass");
		User user2=new User("user2", "pass");
		chatbox.addParticipant(user1);
		chatbox.addParticipant(user2);
		List<User> participants =chatbox.getParticipantsList();
		int boxId=chatbox.getChatBoxID();
		int userId=user1.getUserID();
		assertTrue(msgHandler.removeParticipantFromChatBox(boxId, userId));
		
	}

}
