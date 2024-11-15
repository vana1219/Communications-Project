package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import ServerApp.User.User;

import java.util.List;

public record SendUserList(List<User> userList) implements MessageInterface {
    private static final MessageType type = MessageType.RETURN_USER_LIST;

    public MessageType getType() {
        return type;
    }
}
