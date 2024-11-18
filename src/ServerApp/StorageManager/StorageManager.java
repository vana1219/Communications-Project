package ServerApp.StorageManager;

import ServerApp.ChatBox.ChatBox;
import java.util.HashMap;
import java.io.*;

public class StorageManager {

    // Attributes
    private HashMap<Integer, ChatBox> chatBoxRecords;

    // Constructor
    // *Initializes StorageManager and loads existing chatboxes from file*
    // INPUT: none
    // OUTPUT: none
    public StorageManager() {
        this.chatBoxRecords = new HashMap<>();
        loadChatBoxesFromFile(); // Load existing chatboxes from file
    }

    // Methods

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
        loadChatBoxesFromFile(); // Load chatboxes from file before retrieving
        return chatBoxRecords.get(chatID);
    }

    // *Saves all chatboxes to a file for persistent storage*
    // INPUT: none
    // OUTPUT: none
    private void saveChatBoxesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("chatBoxes.ser"))) {
            oos.writeObject(chatBoxRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // *Loads chatboxes from file into memory*
    // INPUT: none
    // OUTPUT: none
    private void loadChatBoxesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("chatBoxes.ser"))) {
            chatBoxRecords = (HashMap<Integer, ChatBox>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            chatBoxRecords = new HashMap<>(); // If file not found or error occurs, start with an empty HashMap
        }
    }
}