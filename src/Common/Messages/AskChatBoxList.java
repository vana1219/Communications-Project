package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;

public class AskChatBoxList implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.REQUEST_CHATBOX_LIST;

    public MessageType getType() {
        return type;
    }
}