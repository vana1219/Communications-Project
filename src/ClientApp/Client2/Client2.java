package ClientApp.Client2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import Common.Messages.*;
import ServerApp.User.*;

public class Client2
{
	private static Socket client;
	private static boolean loginStatus;
	private static User user;
	
	public static void main(String args[])
	{
		loginStatus = true;
		Scanner userInput = new Scanner(System.in);
		
		try
		{
			System.out.println("Please enter the ip address of the server");
			String userInputStr = userInput.next(); //grab ip address from console
			
			client = new Socket(userInputStr,12345); 
			
			System.out.println("Please enter your username");
			userInputStr = userInput.next(); 
			
			String username = userInputStr;
			
			System.out.println("Please enter your password");
			userInputStr = userInput.next(); 
			
			String password = userInputStr;
			
			Login mylogin = new Login(username, password);
			
			InputStream in = client.getInputStream();
			ObjectInputStream inObj = new ObjectInputStream (in);
			
			OutputStream out = client.getOutputStream();
			ObjectOutputStream outObj = new ObjectOutputStream (out);
			
			outObj.writeObject(mylogin); //sends out a Login object
			
			LoginResponse loginR = (LoginResponse) inObj.readObject();
			
			user = loginR.user();
			
			if (user == null)
			{
				System.out.println("user not found");
			}
			else
			{
				System.out.println("returned username is: " + user.getUsername());
				System.out.println("returned password is: " + user.getPassword());
			}
			
		}
		catch (IOException ex) {
		    System.err.println("I/O error: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
		    System.err.println("Class not found: " + ex.getMessage());
		}
		
		
		
		
		
		userInput.close();
		
	}
	
	
	
}