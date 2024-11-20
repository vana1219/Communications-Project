package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import ServerApp.User.User;

import java.util.List;

public record CreateChat(List<User> participants) implements MessageInterface {
    private static final MessageType type = MessageType.CREATE_CHATBOX;

    public MessageType getType() {
        return type;
    }
}
