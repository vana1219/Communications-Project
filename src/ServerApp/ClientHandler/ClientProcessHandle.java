package ServerApp.ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

import ServerApp.ChatBox.ChatBox;
import ServerApp.Message.Message;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.Server.Server;
import ServerApp.User.User;


public class ClientProcessHandle implements Runnable
{
	private Socket client;
	private Queue<Message> outCont;
	private Queue<Message> inCont;
	private Boolean login;
	
	//Pre: Socket connection must be made, out and in queue must be created prior
	public ClientProcessHandle (Socket cl, Queue<Message> out, Queue<Message> in)
	{
		client = cl;
		outCont = out;
		inCont = in;
		
		login = false; //first login not happened yet
	}
	
	public void run()
	{
		
	}
	
	
	
	
}