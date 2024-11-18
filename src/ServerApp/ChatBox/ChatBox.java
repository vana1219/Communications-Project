package ServerApp.ChatBox;

import ServerApp.User.User;
import ServerApp.Message.Message;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Comparator;

public class ChatBox {

    // Attributes
    private final int chatBoxID;
    private HashSet<User> participants;
    private SortedSet<Message> messages;
    private static int chatBoxCount = 0;

    // Constructor
    // Creates a new ChatBox instance
    public ChatBox() {
        this.chatBoxID = ++chatBoxCount;
        this.participants = new HashSet<>();
        this.messages = new TreeSet<>(Comparator.comparing(Message::getTimestamp)); // Automatically sorts messages in chronological order based on timestamp
    }

    // Getters
    // Returns the unique ChatBox ID
    public int getChatBoxID() {
        return chatBoxID;
    }

    // Returns the set of participants in the ChatBox
    public HashSet<User> getParticipants() {
        return participants;
    }

    // Returns the set of messages in the ChatBox
    public SortedSet<Message> getMessages() {
        return messages;
    }

    // Methods
    // Adds a message to the ChatBox
    // INPUT: message (Message)
    // OUTPUT: none
    public void addMessage(Message message) {
        messages.add(message);
    }

    // Adds a participant to the ChatBox
    // INPUT: user (User)
    // OUTPUT: true if user added successfully, false otherwise
    public boolean addParticipant(User user) {
        return participants.add(user); // Adds the user if not already present in the set
    }

    // Returns a list of all messages in the ChatBox
    public List<Message> getMessagesList() {
        return new ArrayList<>(messages);
    }

    // Returns a list of all participants in the ChatBox
    public List<User> getParticipantsList() {
        return new ArrayList<>(participants);
    }

    // Removes a participant from the ChatBox
    // INPUT: user (User)
    // OUTPUT: true if user removed successfully, false otherwise
    public boolean removeParticipant(User user) {
        return participants.remove(user); // Removes the user if present in the set
    }

    // Creates a new chatbox with specified participants
    // INPUT: List of participants (users)
    // OUTPUT: ChatBox object
    public static ChatBox createChatBox(List<User> participants) {
        ChatBox chatBox = new ChatBox();
        for (User participant : participants) {
            chatBox.addParticipant(participant);
        }
        return chatBox;
    }

    // Returns a copy of the chatbox with no messages (for data safety)
    // INPUT: none
    // OUTPUT: ChatBox object with participants but no messages
    public ChatBox getEmpty() {
        try {
            ChatBox empty = (ChatBox) this.clone();
            empty.messages = new TreeSet<>();
            return empty;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

