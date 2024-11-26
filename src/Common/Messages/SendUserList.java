package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import Common.User.User;

import java.util.List;
import java.io.Serializable;

public record SendUserList(List<User> userList) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.RETURN_USER_LIST;

    public MessageType getType() {
        return type;
    }
}
