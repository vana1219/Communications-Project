package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record BanUser(String userID) implements MessageInterface {
    private static final MessageType type = MessageType.BAN_USER;

    public MessageType getType() {
        return type;
    }
}
