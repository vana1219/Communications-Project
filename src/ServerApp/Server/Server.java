package ServerApp.Server;

import Common.User.User;
import Common.Admin.Admin;
import ServerApp.ClientHandler.ClientHandler;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.StorageManager.StorageManager;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import Common.ChatBox.ChatBox;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server is the main server class responsible for accepting client
 * connections, managing client handlers, and maintaining user and chatbox data
 * in memory.
 */
public class Server {

    // Attributes
    private ServerSocket serverSocket;
    private final List<ClientHandler> clientHandlers;
    private final ConcurrentHashMap<String, String> activeClients; // Tracks active clients (ID -> IP)
    private final MessageHandler messageHandler;
    private final ConcurrentHashMap<Integer, User> userDB;
    private final ConcurrentHashMap<Integer, ChatBox> chatBoxes;
    private final StorageManager storageManager;
    private final AuthenticationSystem authenticationSystem;

    // Constructor initializes the server with necessary components.
    public Server() {
        System.out.println("Initializing Server...");
        this.clientHandlers = new CopyOnWriteArrayList<>();
        this.activeClients = new ConcurrentHashMap<>();
        this.storageManager = new StorageManager();
        this.chatBoxes = new ConcurrentHashMap<>(storageManager.getChatBoxRecords());
        this.authenticationSystem = new AuthenticationSystem(); // Updated to remove file path
        this.userDB = authenticationSystem.getUserDB();
        this.messageHandler = new MessageHandler(storageManager, chatBoxes, userDB, this);

        // Debug logs
        System.out.println("StorageManager initialized with " + chatBoxes.size() + " chatboxes.");
        System.out.println("AuthenticationSystem loaded " + userDB.size() + " users.");
        System.out.println("Server initialization complete.");

        // Create initial users if they don't exist
        createInitialUsers();

        // Create initial chatboxes
        createInitialChatBoxes();
    }

    // Creates initial users (run once)
    private void createInitialUsers() {
        // Check if "Bob Admin" exists
        boolean bobExists = userDB.values().stream().anyMatch(u -> u.getUsername().equals("Bob Admin"));
        Admin bobAdmin;
        if (!bobExists) {
            // Create an Admin object
            bobAdmin = new Admin("Bob Admin", "BobPass", messageHandler, authenticationSystem);
            authenticationSystem.registerUser(bobAdmin);
            System.out.println("Created admin user: Bob Admin");
        } else {
            // Retrieve existing Bob Admin
            bobAdmin = (Admin) userDB.values().stream()
                    .filter(u -> u.getUsername().equals("Bob Admin") && u instanceof Admin)
                    .findFirst()
                    .orElse(null);
        }

        // Now, use bobAdmin to add Sally User
        boolean sallyExists = userDB.values().stream().anyMatch(u -> u.getUsername().equals("Sally User"));
        if (!sallyExists && bobAdmin != null) {
            User sallyUser = new User("Sally User", "SallyPass");
            boolean success = bobAdmin.addUser(sallyUser);
            if (success) {
                System.out.println("Created regular user: Sally User");
            } else {
                System.out.println("Failed to create regular user: Sally User");
            }
        }
    }

    // Creates initial chatboxes (run once)
    private void createInitialChatBoxes() {
        // Check if chatbox between Bob and Sally exists
        boolean chatBoxExists = chatBoxes.values().stream().anyMatch(chatBox -> {
            HashSet<User> participants = chatBox.getParticipants();
            return participants.stream().anyMatch(u -> u.getUsername().equals("Bob Admin")) &&
                   participants.stream().anyMatch(u -> u.getUsername().equals("Sally User"));
        });

        if (!chatBoxExists) {
            // Get Bob and Sally from userDB
            User bob = userDB.values().stream().filter(u -> u.getUsername().equals("Bob Admin")).findFirst().orElse(null);
            User sally = userDB.values().stream().filter(u -> u.getUsername().equals("Sally User")).findFirst().orElse(null);

            if (bob != null && sally != null) {
                List<User> participants = Arrays.asList(bob, sally);
                ChatBox chatBox = messageHandler.createChatBox(participants);
                System.out.println("Created chatbox between Bob Admin and Sally User with ID: " + chatBox.getChatBoxID());
            } else {
                System.out.println("Error: Could not find Bob or Sally to create chatbox.");
            }
        } else {
            System.out.println("Chatbox between Bob and Sally already exists.");
        }
    }

    // Starts the server to begin accepting client connections.
    // INPUT: port (int)
    // OUTPUT: none
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
                    ClientHandler clientHandler = new ClientHandler(
                            client, this, messageHandler, authenticationSystem);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();

                    // Output the number of connected clients
                    System.out.println("Number of connected clients: " + clientHandlers.size());

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
    // INPUT: none
    // OUTPUT: none
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
    // INPUT: handler (ClientHandler)
    // OUTPUT: none
    public void removeClientHandler(ClientHandler handler) {
        clientHandlers.remove(handler);
        String clientIP = handler.getClientSocket().getInetAddress().getHostAddress();
        activeClients.values().removeIf(ip -> ip.equals(clientIP));
        System.out.println("Client disconnected: " + clientIP);

        // Output the number of connected clients
        System.out.println("Number of connected clients: " + clientHandlers.size());
    }

    // Retrieves the list of active client handlers.
    // INPUT: none
    // OUTPUT: List<ClientHandler>
    public List<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    // Retrieves the user database.
    // INPUT: none
    // OUTPUT: ConcurrentHashMap<Integer, User>
    public ConcurrentHashMap<Integer, User> getUserDB() {
        return userDB;
    }

    // Retrieves the chatboxes.
    // INPUT: none
    // OUTPUT: ConcurrentHashMap<Integer, ChatBox>
    public ConcurrentHashMap<Integer, ChatBox> getChatBoxes() {
        return chatBoxes;
    }

    // Main method to start the server.
    public static void main(String[] args) {
        System.out.println("Launching Server application...");
        Server server = new Server();

        // Add a shutdown hook to properly close the server socket on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stopServer();
        }));

        server.startServer(1234);
    }

    // Method to get the external IP address
    // INPUT: none
    // OUTPUT: String (external IP address)
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

