package Common.Messages;

import Common.MessageInterface;
import Common.MessageType;

import static Common.MessageType.LOGIN;

public record Login( String username, String password) implements MessageInterface {
        private static final MessageType type = LOGIN;
        public MessageType getType() {
        return type;
    }
}
