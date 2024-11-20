package ServerApp.StorageManager;

import ServerApp.ChatBox.ChatBox;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

/**
 * StorageManager handles the persistent storage and retrieval of ChatBox objects.
 */
public class StorageManager implements Serializable {
	private static final long serialVersionUID = 1L;
	// Attributes
    private ConcurrentHashMap<Integer, ChatBox> chatBoxRecords;

    // Constructor
    // *Initializes StorageManager and loads existing chatboxes from file*
    // INPUT: none
    // OUTPUT: none
    public StorageManager() {
        this.chatBoxRecords = new ConcurrentHashMap<>();
        loadChatBoxesFromFile(); // Load existing chatboxes from file
    }

    // *Stores a chatbox in memory and saves it to file*
    // INPUT: chatBox (ChatBox)
    // OUTPUT: true if successfully stored, false otherwise
    public boolean storeChatBox(ChatBox chatBox) {
        if (chatBox != null) {
            chatBoxRecords.put(chatBox.getChatBoxID(), chatBox);
            saveChatBoxesToFile(); // Save updated chatboxes to file
            return true;
        }
        return false;
    }

    // *Retrieves a chatbox from storage based on chatID*
    // INPUT: chatID (int)
    // OUTPUT: ChatBox object if found, null otherwise
    public ChatBox retrieveChatBox(int chatID) {
        return chatBoxRecords.get(chatID);
    }

    // *Retrieves all chatbox records from storage*
    // INPUT: none
    // OUTPUT: ConcurrentHashMap of chatBoxID to ChatBox objects
    public ConcurrentHashMap<Integer, ChatBox> getChatBoxRecords() {
        return chatBoxRecords;
    }

    // *Saves all chatboxes to a file for persistent storage*
    // INPUT: none
    // OUTPUT: none
    private void saveChatBoxesToFile() {
        synchronized (this) { // Ensure thread safety during save
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("chatBoxes.ser"))) {
                oos.writeObject(chatBoxRecords);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // *Loads chatboxes from file into memory*
    // INPUT: none
    // OUTPUT: none
    @SuppressWarnings("unchecked")
	private void loadChatBoxesFromFile() {
        synchronized (this) { // Ensure thread safety during load
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("chatBoxes.ser"))) {
                chatBoxRecords = (ConcurrentHashMap<Integer, ChatBox>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                chatBoxRecords = new ConcurrentHashMap<>(); // If file not found or error occurs, start with an empty HashMap
            }
        }
    }
}

