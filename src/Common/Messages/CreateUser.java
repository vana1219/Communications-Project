package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record CreateUser() implements MessageInterface {
    private static final MessageType type = MessageType.CREATE_USER;

    public MessageType getType() {
        return type;
    }
}
