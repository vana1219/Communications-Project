package ServerApp.Server2;

import ServerApp.User.User;
import ServerApp.ClientHandler2.ClientHandler2;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.StorageManager.StorageManager;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.ChatBox.ChatBox;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server2 is the main server class responsible for accepting client
 * connections, managing client handlers, and maintaining user and chatbox data
 * in memory.
 */
public class Server2 {

	// Attributes
	private ServerSocket serverSocket;
	private final List<ClientHandler2> clientHandlers;
	private final ConcurrentHashMap<String, String> activeClients; // Tracks active clients (ID -> IP)
	private final MessageHandler messageHandler;
	private final ConcurrentHashMap<Integer, User> userDB;
	private final ConcurrentHashMap<Integer, ChatBox> chatBoxes;
	private final StorageManager storageManager;
	private final AuthenticationSystem authenticationSystem;

	// Constructor initializes the server with necessary components.
	public Server2() {
		System.out.println("Initializing Server2...");
		this.clientHandlers = new CopyOnWriteArrayList<>();
		this.activeClients = new ConcurrentHashMap<>();
		this.storageManager = new StorageManager();
		this.chatBoxes = new ConcurrentHashMap<>(storageManager.getChatBoxRecords());
		this.authenticationSystem = new AuthenticationSystem("users.ser");
		this.userDB = authenticationSystem.getUserDB();
		this.messageHandler = new MessageHandler(storageManager, chatBoxes, userDB);

		// Debug logs
		System.out.println("StorageManager initialized with " + chatBoxes.size() + " chatboxes.");
		System.out.println("AuthenticationSystem loaded " + userDB.size() + " users.");
		System.out.println("Server2 initialization complete.");
	}

	// Starts the server to begin accepting client connections.
	public void startServer(int port) {
		try {
			serverSocket = new ServerSocket(0); // Automatically assigns an available port
			int assignedPort = serverSocket.getLocalPort();
			System.out.println("Server started on port: " + assignedPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// server is listening on port 1234
			InetAddress localhost = InetAddress.getLocalHost();
			System.out.println("Server IP Address: " + localhost.getHostAddress());

			while (true) {

				// socket object to receive incoming client requests
				Socket client = serverSocket.accept();

				// Displaying that a new client is connected to the server
				System.out.println("New client connected: " + client.getInetAddress().getHostAddress());

				// Create a new ClientHandler2 for the connected client
				ClientHandler2 clientHandler = new ClientHandler2(client, this, messageHandler, authenticationSystem);
				clientHandlers.add(clientHandler);

				// Start the client handler thread
				new Thread(clientHandler).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Removes a client handler from the active list and updates active clients map.
	public void removeClientHandler(ClientHandler2 handler) {
		clientHandlers.remove(handler);
		activeClients.values().removeIf(ip -> ip.equals(handler.getClientSocket().getInetAddress().getHostAddress()));
		System.out.println("Client disconnected: " + handler.getClientSocket().getInetAddress());
	}

	// Retrieves the list of active client handlers.
	public List<ClientHandler2> getClientHandlers() {
		return clientHandlers;
	}

	// Retrieves the user database.
	public ConcurrentHashMap<Integer, User> getUserDB() {
		return userDB;
	}

	// Retrieves the chatboxes.
	public ConcurrentHashMap<Integer, ChatBox> getChatBoxes() {
		return chatBoxes;
	}

	// Main method to start the server.
	public static void main(String[] args) {
		Server2 server = new Server2();
		server.startServer(1234);
	}
}
