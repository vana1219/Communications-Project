package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import ServerApp.User.User;
import ServerApp.ChatBox.ChatBox;

import java.util.List;


public record LoginResponse(User user, List<ChatBox> chatBoxList) implements MessageInterface {
    private static final MessageType type = MessageType.LOGIN_RESPONSE;

    public MessageType getType() {
        return type;
    }
}
