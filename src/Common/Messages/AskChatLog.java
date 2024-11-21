package Common.Messages;

import java.io.Serializable;

import Common.MessageInterface;
import Common.MessageType;

public record AskChatLog(String chatBoxID) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.VIEW_CHATBOX_LOG;

    public MessageType getType() {
        return type;
    }
}
