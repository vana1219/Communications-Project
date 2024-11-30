package Common.Message;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
	@Serial
    private static final long serialVersionUID = 1L;
	// Attributes
	private static int count = 0;
    private int messageID;
    private int senderID;
    private String content;
    private final LocalDateTime timestamp; // immutable once set
    private boolean hidden;

    // Constructor
    public Message(int senderID, String content) {
        this.messageID = ++count;
        this.senderID = senderID;
        this.content = content;
        this.timestamp = LocalDateTime.now(); // this creates a timestamp during creation of message that can NOT be changed 
        this.hidden = false;
    }

    // Getters and Setters
    // NOTE: we ONLY have getters for messageID, senderID, content + timestamp since they are immutable once created 
    // only "hidden" can be changed 
    public int getMessageID() {
        return messageID;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }


    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return  content;
    }
}
