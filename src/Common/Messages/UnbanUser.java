package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record UnbanUser(String userID) implements MessageInterface {
    private static final MessageType type = MessageType.UNBAN_USER;

    public MessageType getType() {
        return type;
    }
}
