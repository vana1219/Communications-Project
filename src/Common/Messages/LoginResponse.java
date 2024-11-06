package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

import java.util.List;


public record LoginResponse(ServerApp.User.User user,
                            List<ServerApp.ChatBox.ChatBox> chatBoxList) implements MessageInterface {
    private static final MessageType type = MessageType.LOGIN_RESPONSE;

    public MessageType getType() {
        return type;
    }
}
