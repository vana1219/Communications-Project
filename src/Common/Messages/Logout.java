package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record Logout() implements MessageInterface {
    private static final MessageType type = MessageType.LOGOUT;

    public MessageType getType() {
        return type;
    }
}
