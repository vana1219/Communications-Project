package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import ServerApp.ChatBox.ChatBox;

public record SendChatBox(ChatBox chatBox) implements MessageInterface {
    private static final MessageType type = MessageType.RETURN_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
