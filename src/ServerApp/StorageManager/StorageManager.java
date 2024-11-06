package ServerApp.StorageManager;

import ServerApp.ChatBox.ChatBox;
import java.util.HashMap;
import java.io.*;

public class StorageManager {

    // Attributes
    private HashMap<Integer, ChatBox> chatBoxRecords;

    // Constructor
    public StorageManager() {
        this.chatBoxRecords = new HashMap<>();
        loadChatBoxesFromFile(); // Load existing chatboxes from file
    }

    // Methods
    public boolean storeChatBox(ChatBox chatBox) {
        chatBoxRecords.put(chatBox.getChatBoxID(), chatBox);
        saveChatBoxesToFile(); // Save updated chatboxes to file
        return true;
    }

    public ChatBox retrieveChatBox(int chatID) {
        loadChatBoxesFromFile(); // Load chatboxes from file before retrieving
        return chatBoxRecords.get(chatID);
    }

    private void saveChatBoxesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("chatBoxes.ser"))) {
            oos.writeObject(chatBoxRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatBoxesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("chatBoxes.ser"))) {
            chatBoxRecords = (HashMap<Integer, ChatBox>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            chatBoxRecords = new HashMap<>(); // If file not found or error occurs, start with an empty HashMap
        }
    }
}

