
package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;
import ServerApp.Message.Message;

public record SendMessage(Message message, int chatBoxID) implements MessageInterface {
    private static final MessageType type = MessageType.SEND_MESSAGE;

    public MessageType getType() {
        return type;
    }
}
