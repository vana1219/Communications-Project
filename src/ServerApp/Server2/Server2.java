package ServerApp.Server2;

import ServerApp.User.User;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.StorageManager.StorageManager;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.ClientHandler2.ClientHandler2;
import ServerApp.ChatBox.ChatBox;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

/**
 * Server2 is the main server class responsible for accepting client connections,
 * managing client handlers, and maintaining user and chatbox data in memory.
 */
public class Server2 {

    // Attributes
    private ServerSocket serverSocket;
    private final List<ClientHandler2> clientHandlers;
    private int port;
    private final MessageHandler messageHandler;
    private final ConcurrentHashMap<Integer, User> userDB;
    private final ConcurrentHashMap<Integer, ChatBox> chatBoxes;
    private final StorageManager storageManager;
    private final AuthenticationSystem authenticationSystem;
    private volatile boolean isRunning;

    /**
     * Constructor initializes the server with necessary components.
     */
    public Server2(int port) {
        this.port = port;
        this.clientHandlers = new CopyOnWriteArrayList<>();
        this.storageManager = new StorageManager();
        this.chatBoxes = new ConcurrentHashMap<>(storageManager.getChatBoxRecords());
        this.authenticationSystem = new AuthenticationSystem("users.ser");
        this.userDB = authenticationSystem.getUserDB(); // Directly reference the ConcurrentHashMap from AuthenticationSystem
        this.messageHandler = new MessageHandler(storageManager, chatBoxes, userDB);
        this.isRunning = false;
    }

    // *Starts the server to begin accepting client connections*
    // INPUT: none
    // OUTPUT: none
    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server2 started on port " + port);

            // Main loop to accept clients
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());

                    // Create a new ClientHandler2 for the connected client
                    ClientHandler2 clientHandler = new ClientHandler2(clientSocket, this, messageHandler, authenticationSystem);
                    clientHandlers.add(clientHandler);

                    // Start the client handler thread
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start Server2 on port " + port + ": " + e.getMessage());
        } finally {
            shutdownServer();
        }
    }

    // *Shuts down the server gracefully by closing the server socket and all client handlers*
    // INPUT: none
    // OUTPUT: none
    public void shutdownServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server2 socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing Server2 socket: " + e.getMessage());
        }

        // Close all client handlers
        for (ClientHandler2 handler : clientHandlers) {
            handler.closeConnection();
        }
        clientHandlers.clear();
        System.out.println("Server2 shutdown complete.");
    }

    // *Removes a client handler from the active list*
    // INPUT: handler (ClientHandler2)
    // OUTPUT: none
    public void removeClientHandler(ClientHandler2 handler) {
        clientHandlers.remove(handler);
        System.out.println("Client disconnected: " + handler.getClientSocket().getInetAddress());
    }

    // *Retrieves the list of active client handlers*
    // INPUT: none
    // OUTPUT: List of ClientHandler2 instances
    public List<ClientHandler2> getClientHandlers() {
        return clientHandlers;
    }

    // *Retrieves the user database*
    // INPUT: none
    // OUTPUT: ConcurrentHashMap of userID to User objects
    public ConcurrentHashMap<Integer, User> getUserDB() {
        return userDB;
    }

    // *Retrieves the chatboxes*
    // INPUT: none
    // OUTPUT: ConcurrentHashMap of chatBoxID to ChatBox objects
    public ConcurrentHashMap<Integer, ChatBox> getChatBoxes() {
        return chatBoxes;
    }

    // *Main method to start the server*
    // INPUT: args (String[] - optional port number)
    // OUTPUT: none
    public static void main(String[] args) {
        int port = 12345; // Default port
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + port);
            }
        }
        Server2 server = new Server2(port);
        server.startServer();
    }
}
