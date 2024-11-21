package Common.Messages;

import java.io.Serializable;

import Common.MessageInterface;
import Common.MessageType;


public record AskUserList() implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.REQUEST_USER_LIST;

    public MessageType getType() {
        return type;
    }
}
