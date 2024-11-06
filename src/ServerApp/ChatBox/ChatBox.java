package src.ServerApp.ChatBox;

import src.ServerApp.User.User;
import src.ServerApp.Message.Message;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.TreeSet;

public class ChatBox {

    // Attributes
    private final int chatBoxID;
    private final String chatName;
    private HashSet<User> participants;
    private SortedSet<Message> messages;
    private static int chatBoxCount = 0;

    // Constructor
    public ChatBox(String chatName) {
        this.chatName = chatName;
        this.chatBoxID = ++chatBoxCount;
        this.participants = new HashSet<>();
        this.messages = new TreeSet<>((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp())); // Automatically sorts messages in chronological order based on timestamp
    }

    // Getters and Setters
    public int getChatBoxID() {
        return chatBoxID;
    }
    
    public String getChatName() {
        return chatName;
    }

    public HashSet<User> getParticipants() {
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
        return participants.add(user); // Adds the user if not already present in the set
    }

    public List<Message> getMessagesList() {
        return new ArrayList<>(messages);
    }

    public List<User> getParticipantsList() {
        return new ArrayList<>(participants);
    }

    public boolean removeParticipant(User user) {
        return participants.remove(user); // Removes the user if present in the set
    }
}
