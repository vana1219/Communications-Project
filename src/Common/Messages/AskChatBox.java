package Common.Messages;

import java.io.Serializable;

import Common.MessageInterface;
import Common.MessageType;

public record AskChatBox(String chatBoxID) implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.REQUEST_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
