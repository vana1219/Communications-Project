package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record AskChatBox(String chatBoxID) implements MessageInterface {
    private static final MessageType type = MessageType.REQUEST_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
