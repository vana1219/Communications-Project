package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import Common.User.User;

import java.util.List;
import java.io.Serializable;

public record CreateChat(List<User> participants) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.CREATE_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
