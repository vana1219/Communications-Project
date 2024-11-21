package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public record CreateUser(String username, String password, boolean isAdmin) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.CREATE_USER;

    public MessageType getType() {
        return type;
    }
}
