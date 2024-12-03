package Testing;


import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Common.ChatBox.ChatBox;
import Common.User.User;
import Common.Message.Message;

class TestChatBox {
    private ChatBox chatBox;
    private User user1;
    private User user2;
    private Message message;
    private HashSet<User> participants;


    @BeforeEach
    public void startChatBox() {
    	ChatBox.resetChatBoxIdGenerator(); 
        user1 = new User("Sally", "pass123");
        user2 = new User("Bob", "pass456");
        chatBox = new ChatBox("Test ChatBox");
        this.participants = new HashSet<>();
    }
    
    @Test
    void testConstructor() {
    	assertNotNull(chatBox);
    }
    
    @Test
    void testGetChatBoxNameandGetChatBoxID() {
    	assertEquals("Test ChatBox",chatBox.getName());
    	assertEquals(1, chatBox.getChatBoxID());
    }
    
    @Test 
    void testGetParrticipants() {
    	chatBox.addParticipant(user1);
    	chatBox.addParticipant(user2);
    	chatBox.setParticipants(participants);
    	assertNotNull(chatBox.getParticipants());
    }
    
    @Test 
    void testRemoveParticiapant() {
    	chatBox.addParticipant(user1);
    	assertTrue(chatBox.removeParticipant(user1));
    }
    
    @Test 
    void testGetChatBoxEmpty() {
    	ChatBox emptyChatBox = chatBox.getEmpty();
    	assertNotNull(emptyChatBox);
    	assertEquals(chatBox.getChatBoxID(), emptyChatBox.getChatBoxID());
    	assertEquals(chatBox.getParticipants(), emptyChatBox.getParticipants());
    }
}

