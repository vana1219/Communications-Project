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
            serverSocket = new ServerSocket(port);
            // Get the external IP address
            String serverIP = getExternalIPAddress();
            System.out.println("Server started on IP: " + serverIP + ", port: " + port);

            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("New client connected: " + client.getInetAddress().getHostAddress());
                    ClientHandler2 clientHandler = new ClientHandler2(
                            client, this, messageHandler, authenticationSystem);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                    // Continue accepting new clients
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                    // Continue accepting new clients
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during server startup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stopServer(); // Ensure the ServerSocket is closed
        }
    }

    // Stops the server and closes the server socket
    public void stopServer() {
        System.out.println("Stopping server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Removes a client handler from the active list and updates active clients map.
    public void removeClientHandler(ClientHandler2 handler) {
        clientHandlers.remove(handler);
        String clientIP = handler.getClientSocket().getInetAddress().getHostAddress();
        activeClients.values().removeIf(ip -> ip.equals(clientIP));
        System.out.println("Client disconnected: " + clientIP);
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
        System.out.println("Launching Server2 application...");
        Server2 server = new Server2();

        // Add a shutdown hook to properly close the server socket on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stopServer();
        }));

        server.startServer(1234);
    }

    // Method to get the external IP address
    private String getExternalIPAddress() {
        String externalIP = "Unknown IP";
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            externalIP = socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            System.err.println("Error obtaining external IP address: " + e.getMessage());
            e.printStackTrace();
        }
        return externalIP;
    }
}
