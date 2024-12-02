package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record AskChatLog(int chatBoxID) implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.VIEW_CHATBOX_LOG;

    public MessageType getType() {
        return type;
    }
}