package ServerApp.MessageHandler;

import ServerApp.StorageManager.StorageManager;
import ServerApp.ChatBox.ChatBox;
import ServerApp.Message.Message;
import ServerApp.Server.Server;
import ServerApp.ClientHandler.ClientHandler;
import ServerApp.User.User;
import java.util.HashMap;
import java.util.List;

public class MessageHandler {

    // Attributes
    private StorageManager storageManager; // Manages storage operations for chatboxes
    private HashMap<Integer, ChatBox> chatBoxes; // Keeps track of all chatboxes currently loaded in memory

    // Constructor
    public MessageHandler(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.chatBoxes = new HashMap<>();
    }

    // Sends a message to a specific chatbox
    // INPUT: chatBoxID, message
    // OUTPUT: true if successful, false otherwise
    public boolean sendMessage(int chatBoxID, Message message) {
        ChatBox chatBox = chatBoxes.get(chatBoxID);
        if (chatBox != null) {
            chatBox.addMessage(message);
            storageManager.storeChatBox(chatBox); // Store updated chatbox in persistent storage
            notifyParticipants(chatBoxID, message); // Notify all participants of the new message
            return true;
        }
        return false;
    }

    // Adds a user to a specific chatbox
    // INPUT: chatBoxID, clientHandler
    // OUTPUT: true if user added successfully, false otherwise
    public boolean addParticipantToChatBox(int chatBoxID, ClientHandler clientHandler) {
        ChatBox chatBox = chatBoxes.get(chatBoxID);
        if (chatBox != null) {
            boolean added = chatBox.addParticipant(clientHandler.getUser());
            if (added) {
                storageManager.storeChatBox(chatBox); // Store updated chatbox to reflect the new participant
                return true;
            }
        }
        return false;
    }

    // Removes a user from a specific chatbox
    // INPUT: chatBoxID, userID
    // OUTPUT: true if user removed successfully, false otherwise
    public boolean removeParticipantFromChatBox(int chatBoxID, int userID) {
        ChatBox chatBox = chatBoxes.get(chatBoxID);
        if (chatBox != null) {
            User userToRemove = chatBox.getParticipants().stream().filter(user -> user.getUserID() == userID).findFirst().orElse(null);
            boolean removed = chatBox.removeParticipant(userToRemove);
            if (removed) {
                storageManager.storeChatBox(chatBox); // Store updated chatbox to reflect the removed participant
                return true;
            }
        }
        return false;
    }

    // Retrieves all messages for a specific chatbox
    // INPUT: chatBoxID
    // OUTPUT: List of messages or null if chatbox not found
    public List<Message> getMessages(int chatBoxID) {
        ChatBox chatBox = chatBoxes.get(chatBoxID);
        if (chatBox != null) {
            return chatBox.getMessagesList();
        }
        return null;
    }

    // Notifies all participants in the chatbox of a new message
    // INPUT: chatBoxID, message
    // OUTPUT: none
    private void notifyParticipants(int chatBoxID, Message message) {
        ChatBox chatBox = chatBoxes.get(chatBoxID);
        if (chatBox != null) {
            for (User participant : chatBox.getParticipants()) {
                ClientHandler clientHandler = findClientHandler(participant.getUserID());
                if (clientHandler != null) {
                    clientHandler.sendMessage(message, chatBoxID); // Send the message to the client via the client handler
                }
            }
        }
    }

    // Finds the ClientHandler for a given user ID
    // INPUT: userID
    // OUTPUT: ClientHandler or null if not found
    private ClientHandler findClientHandler(int userID) {
        // Iterate through client handlers to find the matching userID
        for (ClientHandler handler : Server.getClientHandlers()) {
            if (handler.getUser().getUserID() == userID) {
                return handler;
            }
        }
        return null;
    }

    // Loads a chatbox into memory from storage
    // INPUT: chatBoxID
    // OUTPUT: Loaded ChatBox or null if not found
    public ChatBox loadChatBox(int chatBoxID) {
        ChatBox chatBox = storageManager.retrieveChatBox(chatBoxID);
        if (chatBox != null) {
            chatBoxes.put(chatBoxID, chatBox); // Add the chatbox to the in-memory collection
        }
        return chatBox;
    }

    // Stores a chatbox in persistent storage
    // INPUT: chatBox
    // OUTPUT: true if stored successfully, false otherwise
    public boolean storeChatBox(ChatBox chatBox) {
        if (chatBox != null) {
            chatBoxes.put(chatBox.getChatBoxID(), chatBox); // Update the in-memory collection
            storageManager.storeChatBox(chatBox); // Store the chatbox in persistent storage
            return true;
        }
        return false;
    }
}