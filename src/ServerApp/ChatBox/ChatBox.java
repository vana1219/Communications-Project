package ServerApp.ChatBox;

import ServerApp.User.User;
import ServerApp.Message.Message;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.Comparator;
import java.io.Serializable;

public class ChatBox implements Serializable {
	private static final long serialVersionUID = 1L;
	// Attributes
    private final int chatBoxID;
    private HashSet<User> participants;
    private SortedSet<Message> messages;
    private static int chatBoxCount = 0;
    private boolean isHidden;

    // Serializable Comparator
    private static final Comparator<Message> MESSAGE_TIMESTAMP_COMPARATOR = new SerializableComparator();

    private static class SerializableComparator implements Comparator<Message>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Message m1, Message m2) {
            return m1.getTimestamp().compareTo(m2.getTimestamp());
        }
    }

    // Constructor
    public ChatBox() {
        this.chatBoxID = ++chatBoxCount;
        this.participants = new HashSet<>();
        this.messages = new TreeSet<>(MESSAGE_TIMESTAMP_COMPARATOR);
        this.isHidden = false;
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

    // Returns the hidden status of the ChatBox
    public boolean isHidden() {
        return isHidden;
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
    // INPUT: none
    // OUTPUT: List of messages
    public List<Message> getMessagesList() {
        return new ArrayList<>(messages);
    }

    // Returns a list of all participants in the ChatBox
    // INPUT: none
    // OUTPUT: List of participants
    public List<User> getParticipantsList() {
        return new ArrayList<>(participants);
    }

    // Removes a participant from the ChatBox
    // INPUT: user (User)
    // OUTPUT: true if user removed successfully, false otherwise
    public boolean removeParticipant(User user) {
        return participants.remove(user); // Removes the user if present in the set
    }

    // Hides the ChatBox from users
    // INPUT: none
    // OUTPUT: none
    public void hideChatBox() {
        this.isHidden = true;
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