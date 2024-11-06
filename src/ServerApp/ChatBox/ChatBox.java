package src.ServerApp.ChatBox;

import src.ServerApp.User.User;
import src.ServerApp.Message.Message;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.TreeSet;

public class ChatBox {

    // Attributes
    private final int chatBoxID;
    private HashMap<String, User> participants;
    private SortedSet<Message> messages;
    private static int chatBoxCount = 0;

    // Constructor
    public ChatBox() {
        this.chatBoxID = ++chatBoxCount;
        this.participants = new HashMap<>();
        this.messages = new TreeSet<>((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp())); // Automatically sorts messages in chronological order based on timestamp
    }

    // Getters and Setters
    public int getChatBoxID() {
        return chatBoxID;
    }

    public HashMap<String, User> getParticipants() {
        return participants;
    }

    public SortedSet<Message> getMessages() {
        return messages;
    }

    // Methods
    public void addMessage(Message message) {
        messages.add(message);
    }

    public boolean addParticipant(User user) {
        if (!participants.containsKey(user.getUserID())) {
            participants.put(user.getUserID(), user);
            return true;
        }
        return false;
    }

    public List<Message> getMessagesList() {
        return new ArrayList<>(messages);
    }

    public List<User> getParticipantsList() {
        return new ArrayList<>(participants.values());
    }

    public boolean removeParticipant(String userID) {
        if (participants.containsKey(userID)) {
            participants.remove(userID);
            return true;
        }
        return false;
    }
}
