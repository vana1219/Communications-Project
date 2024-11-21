package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record Notification(String text) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.NOTIFICATION;

    public MessageType getType() {
        return type;
    }
}
