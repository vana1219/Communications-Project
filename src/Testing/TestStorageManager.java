package Testing;

import static org.junit.jupiter.api.Assertions.*;

import ServerApp.StorageManager.StorageManager;
import Common.ChatBox.ChatBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestStorageManager {
    private StorageManager storageManager;

    @BeforeEach
    void startStorageManager() {
        storageManager = new StorageManager();
    }

    @Test
    void testStoreAndRetrieveChatBox() {
        ChatBox chatBox = new ChatBox("ChatBox1");
        assertTrue(storageManager.storeChatBox(chatBox));
        
        ChatBox retrievedChatBox = storageManager.retrieveChatBox(chatBox.getChatBoxID());
        assertNotNull(retrievedChatBox);
        assertEquals(chatBox.getChatBoxID(), retrievedChatBox.getChatBoxID());
    }
    
}
