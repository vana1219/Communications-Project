package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;


public record AskUserList() implements MessageInterface {
    private static final MessageType type = MessageType.REQUEST_USER_LIST;

    public MessageType getType() {
        return type;
    }
}
