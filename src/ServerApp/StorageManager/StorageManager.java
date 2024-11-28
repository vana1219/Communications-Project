package ServerApp.StorageManager;

import Common.ChatBox.ChatBox;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.nio.file.*;

/**
 * StorageManager handles the persistent storage and retrieval of ChatBox objects.
 */
public class StorageManager implements Serializable {
    private static final long serialVersionUID = 1L;

    // Attributes
    private final ConcurrentHashMap<Integer, ChatBox> chatBoxRecords;
    private final String chatBoxesDirectory;

    // Constructor
    // Initializes StorageManager and loads existing chatboxes from files
    public StorageManager() {
        this.chatBoxRecords = new ConcurrentHashMap<>();
        this.chatBoxesDirectory = "chatboxes"; // Directory to store chatbox files
        createChatBoxesDirectory();
        loadChatBoxesFromFiles(); // Load existing chatboxes from files
    }

    // Creates the chatboxes directory if it doesn't exist
    private void createChatBoxesDirectory() {
        File directory = new File(chatBoxesDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdir();
            if (created) {
                System.out.println("ChatBoxes directory created.");
            } else {
                System.err.println("Failed to create ChatBoxes directory.");
            }
        }
    }

    // Stores a chatbox in memory and saves it to an individual file
    public boolean storeChatBox(ChatBox chatBox) {
        if (chatBox != null) {
            chatBoxRecords.put(chatBox.getChatBoxID(), chatBox);
            saveChatBoxToFile(chatBox); // Save the chatbox to its individual file
            return true;
        }
        return false;
    }

    // Retrieves a chatbox from storage based on chatID
    public ChatBox retrieveChatBox(int chatID) {
        return chatBoxRecords.get(chatID);
    }

    // Retrieves all chatbox records from storage
    public ConcurrentHashMap<Integer, ChatBox> getChatBoxRecords() {
        return chatBoxRecords;
    }

    // Saves a single chatbox to its individual file
    private void saveChatBoxToFile(ChatBox chatBox) {
        synchronized (this) { // Ensure thread safety during save
            String fileName = chatBoxesDirectory + File.separator + chatBox.getChatBoxID(); // Filename is chatBoxID
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
                oos.writeObject(chatBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Loads chatboxes from individual files into memory
    private void loadChatBoxesFromFiles() {
        synchronized (this) { // Ensure thread safety during load
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(chatBoxesDirectory))) {
                for (Path path : directoryStream) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                        ChatBox chatBox = (ChatBox) ois.readObject();
                        chatBoxRecords.put(chatBox.getChatBoxID(), chatBox);
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error loading chatbox from file: " + path.getFileName());
                        e.printStackTrace();
                    }
                }
                System.out.println("Loaded " + chatBoxRecords.size() + " chatboxes from files.");
            } catch (IOException e) {
                System.err.println("Error reading chatbox files from directory: " + chatBoxesDirectory);
                e.printStackTrace();
            }
        }
    }
}
