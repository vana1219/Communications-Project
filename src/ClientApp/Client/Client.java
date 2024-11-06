package ClientApp.Client;

import ServerApp.ChatBox.ChatBox;
import ServerApp.Message.Message;
import ServerApp.User.User;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Queue;

public class Client {
	private String serverAddress;
	private DataInputStream inputStream;
	private Socket socket;
	private int port;
	private Queue<Message> messageQueue;
	private User userData;
	private HashSet<ChatBox> ChatBoxList;

    public static void main(String[] args) {

    }

//testing commit

public DataInputStream ClientMessageThread() {
    return null;
}

public void sendMessageRequest() {

}

public void sendChatBoxRequest() {

}

public void sendUnbanUserRequest() {

}

public void sendResetUserPassword() {

}

public void sendCreateUserRequest() {

}

public String receiveResponse() {
	return null;
}

public void receiveChatBoxStatus() {

}

public void sendViewChatBoxLogRequest() {

}

public void receiveChatBoxLog() {

}}