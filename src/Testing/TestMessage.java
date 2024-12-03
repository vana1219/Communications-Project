package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Common.Message.Message;

class TestMessage {
	private Message message;
	private int senderID;
	private String content;

	
	// Create Message object to test, id = 1
	@BeforeEach
	public void setUpMessage() {
		message= new Message(1,"This is a New Message");
	}
	
	@Test
	void testMessageConstructorSenderIDandtext() {
		// Make sure the object isn't null
		assertNotNull(message);
		assertEquals(1, message.getSenderID());
		assertEquals("This is a New Message", message.getContent());
		
	}
	@Test
	void testMessageConstructorwithObjectMessage() {
		Message newMessage=new Message(message);
		assertNotNull(newMessage);
		assertEquals(1, newMessage.getSenderID());
		assertEquals("This is a New Message",newMessage.getContent());
	}
	
	// Check that the senders ID is correct
	@Test
	void testGetSenderID() {
		assertEquals(1, message.getSenderID());
	}
	
	// Get the Message id and check 
	@Test
	void testGetMessageID() {
		Message newMessage=new Message(message);	
		System.out.println(message.getMessageID());
		assertEquals(4, message.getMessageID());
	}
	
	// Validate the contents of the message
	@Test
	void testGetConTent() {
		assertEquals("This is a New Message",message.getContent());
	}
	

}
