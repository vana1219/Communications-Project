package ServerApp.ClientHandler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Queue;

import Common.Message.Message;


public class ClientMessageSend implements Runnable
{
	private Socket client;
	private Queue<Message> OutBoundQue;
	
	public ClientMessageSend(Socket cl, Queue<Message> out)
	{
		client = cl;
		OutBoundQue = out;
	}
	
	public void run()
	{
		try
		{
			OutputStream outSt = client.getOutputStream();
			ObjectOutputStream objOutStream = new ObjectOutputStream (outSt);
			Message temp;
			
			while (true)// need to change to work with message handler
			{
				if ( !OutBoundQue.isEmpty())
				{
					temp = OutBoundQue.remove();
					
					objOutStream.writeObject(temp); //send object back to client
				}
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
}