package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record Notification(String text) implements MessageInterface {
    private static final MessageType type = MessageType.NOTIFICATION;

    public MessageType getType() {
        return type;
    }
}
