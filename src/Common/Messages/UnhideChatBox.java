package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record UnhideChatBox(int chatBoxID) implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.UNHIDE_CHATBOX;

    @Override
    public MessageType getType() {
        return type;
    }
}