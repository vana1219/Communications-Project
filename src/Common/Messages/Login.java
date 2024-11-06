package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

public record Login(MessageType type, String username, String password) implements MessageInterface {

        public MessageType getType() {
        return type;
    }
}
