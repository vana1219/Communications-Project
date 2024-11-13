package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record SendChatLog(String chatBoxLog) implements MessageInterface {
    private static final MessageType type = MessageType.RETURN_CHATBOX_LOG;

    public MessageType getType() {
        return type;
    }
}
