package ServerApp.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import ServerApp.ClientHandler.ClientHandler;
import ServerApp.User.User;


public class Server {
	
	private String ipAddress;
	private List<ClientHandler> clientHandler;
	private MessageHandler messageHandler;
	private HashMap<String, User> userDB;
	private static ServerSocket serverSocket=null;
	private int port;
	
	
	public static void main(String[] args) {
		int default_port=3000;
		
		Server server = new Server();
		server.setPort(default_port);
		try{
			server.startServer(default_port);
		 
			while(true) {
				try {
					server.acceptClient();
					}
				catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }	
			 }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			server.shutDownServer();
			}
		}

	public void setPort(int port) {
		this.port=port;
	}
	public int getPort() {
		return this.port;
	}
	public void startServer(int port) throws IOException{
		serverSocket = new ServerSocket(port);		//creates a new ServerSocket object that listens for incoming connections
		System.out.println("The server is running in port: "+ port);
		serverSocket.setReuseAddress(true);
		
	}
	public void acceptClient() throws IOException{
		try {
			Socket client=serverSocket.accept();
			 
			// Displaying that new client is connected to server
			System.out.println("New client connected " + client);
			
			// create a new thread object
			ClientHandler clientSock = new ClientHandler(client);

			// This thread will handle the client separately
			new Thread(clientSock).start();
			}
		catch (IOException e) {
            System.err.println("Error accepting client connection: " + e.getMessage());
        }	
	}
	public void shutDownServer() {
		if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server shut down successfully.");
            } catch (IOException e) {
                System.err.println("Error shutting down server: " + e.getMessage());
                e.printStackTrace();
            }
        }
	}
  
	}
