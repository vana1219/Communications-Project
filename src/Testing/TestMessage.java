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

	@BeforeEach
	public void setUpMessage() {
		message= new Message(1,"This is a New Message");
	}
	@Test
	void testMessageConstructorSenderIDandtext() {
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
	@Test
	void testGetSenderID() {
		assertEquals(1, message.getSenderID());
	}
	@Test
	void testGetMessageID() {
		Message newMessage=new Message(message);	
		System.out.println(message.getMessageID());
		assertEquals(4, message.getMessageID());
	}
	@Test
	void testGetConTent() {
		assertEquals("This is a New Message",message.getContent());
	}
	

}
