package Common.Messages;

import Common.ChatBox.ChatBox;
import Common.MessageInterface;
import Common.MessageType;
import java.io.Serializable;
import java.util.List;

public class SendChatBoxList implements MessageInterface, Serializable {
    private static final MessageType type = MessageType.RETURN_CHATBOX_LIST;
    private final List<ChatBox> chatBoxes;

    public SendChatBoxList(List<ChatBox> chatBoxes) {
        this.chatBoxes = chatBoxes;
    }

    public List<ChatBox> getChatBoxes() {
        return chatBoxes;
    }

    public MessageType getType() {
        return type;
    }
}