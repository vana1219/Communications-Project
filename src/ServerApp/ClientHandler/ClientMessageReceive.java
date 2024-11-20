package ServerApp.ClientHandler;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Queue;

import ServerApp.Message.Message;

public class ClientMessageReceive implements Runnable
{
	private Socket client;
	private Queue<Message> inCont;
	
	public ClientMessageReceive(Socket cl, Queue<Message> in)
	{
		client = cl;
		inCont = in;
		
		
	}
	
	public void run()
	{
		Message temp;
	
		
		
		try
		{
			
			InputStream istream = client.getInputStream();
			ObjectInputStream objectStrIn = new ObjectInputStream (istream);
			
			while (true)
			{
				temp =  (Message) objectStrIn.readObject(); //read object from client
				
				
				inCont.add(temp);
				
			}
			
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch( ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
}