package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

import java.io.Serializable;

public record LogoutResponse() implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.LOGOUT_RESPONSE;
    public MessageType getType() {
            return type;
    }
}
