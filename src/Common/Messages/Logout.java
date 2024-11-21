package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record Logout() implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.LOGOUT;

    public MessageType getType() {
        return type;
    }
}
