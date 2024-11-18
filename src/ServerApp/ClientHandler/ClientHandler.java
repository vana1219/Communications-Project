package ServerApp.ClientHandler;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

import ServerApp.ChatBox.ChatBox;
import ServerApp.Message.Message;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.Server.Server;
import ServerApp.User.User;

public class ClientHandler{
	private final Socket clientSocket;
	private OutputStream outputStream;
	private ObjectOutputStream objectOutput;
	private InputStream inputStream;
	private ObjectInputStream objectInput;
	private User user;
	private Server server;
	private MessageHandler messageHandler;
	private Map<String, ChatBox> chatBoxList;
    
    private ClientProcessHandle processHandle;
    private Queue<Message> OutBoundQue;
    private Queue<Message> InBoundQue;
    private ClientMessageRecieve clientMsgRecieve;
    private ClientMessageSend clientMsgSend;
	
	
	//Constructor
	public ClientHandler(Socket socket) {
		this.clientSocket=socket;
//		this.clientMsgRecieve= new ClientMessageRecieve();
//		this.clientMsgSend= new ClientMessageSend();
		
	}

	public User getUser()
	{
		return user;
	}
	
	public Message recieveMessage() {
		return null;
	}
	public void processRequest(Message request) {
		
	}
	public void sendResponse(String response) {
		//Change Response to String for now
		//Will talk about that later
	}
	public void sendMessage(Message message, int chatBoxID) {
		
	}
	public void sendChatBoxLog(int chatBoxID, String chatBoxLog) {
		
	}
	public void acknowledgeDelivery(String messageID) {
		
	}
	public void closeConnection() {
		
	}
}