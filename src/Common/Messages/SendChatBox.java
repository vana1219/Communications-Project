package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import Common.ChatBox.ChatBox;
import java.io.Serializable;

public record SendChatBox(ChatBox chatBox) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.RETURN_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
