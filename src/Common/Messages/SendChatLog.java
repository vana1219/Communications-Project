package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record SendChatLog(String chatBoxLog) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.RETURN_CHATBOX_LOG;

    public MessageType getType() {
        return type;
    }
}
