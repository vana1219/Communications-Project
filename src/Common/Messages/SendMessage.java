
package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import Common.Message.Message;
import java.io.Serializable;

public record SendMessage(Message message, int chatBoxID) implements MessageInterface,Serializable {
    private static final MessageType type = MessageType.SEND_MESSAGE;

    public MessageType getType() {
        return type;
    }
}
