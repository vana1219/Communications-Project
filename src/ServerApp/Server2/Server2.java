package ServerApp.Server2;

import ServerApp.User.User;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.StorageManager.StorageManager;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.ClientHandler2.ClientHandler2;
import ServerApp.ChatBox.ChatBox;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server2 is the main server class responsible for accepting client connections,
 * managing client handlers, and maintaining user and chatbox data in memory.
 */
public class Server2 {

    // Attributes
    private ServerSocket serverSocket;
    private final List<ClientHandler2> clientHandlers;
    private final ConcurrentHashMap<String, String> activeClients; // Tracks active clients (ID -> IP)
    private final int port;
    private final MessageHandler messageHandler;
    private final ConcurrentHashMap<Integer, User> userDB;
    private final ConcurrentHashMap<Integer, ChatBox> chatBoxes;
    private final StorageManager storageManager;
    private final AuthenticationSystem authenticationSystem;
    private volatile boolean isRunning;

   
	//vConstructor initializes the server with necessary components.
    public Server2(int port) {
        System.out.println("Initializing Server2...");
        this.port = port;
        this.clientHandlers = new CopyOnWriteArrayList<>();
        this.activeClients = new ConcurrentHashMap<>();
        this.storageManager = new StorageManager();
        this.chatBoxes = new ConcurrentHashMap<>(storageManager.getChatBoxRecords());
        this.authenticationSystem = new AuthenticationSystem("users.ser");
        this.userDB = authenticationSystem.getUserDB();
        this.messageHandler = new MessageHandler(storageManager, chatBoxes, userDB);
        this.isRunning = false;

        // Debug logs
        System.out.println("StorageManager initialized with " + chatBoxes.size() + " chatboxes.");
        System.out.println("AuthenticationSystem loaded " + userDB.size() + " users.");
        System.out.println("Server2 initialization complete.");
    }

    // Starts the server to begin accepting client connections.
    public void startServer() {
        try {
            String serverIP = getServerIpAddress();
            System.out.println("Server IP: " + serverIP);
            System.out.println("Starting Server2 on port " + port);

            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true); // Allow port reuse
            try {
                serverSocket.bind(new InetSocketAddress(port)); // Bind to the specified port
            } catch (BindException e) {
                System.err.println("Port " + port + " is already in use. Trying next available port...");
                serverSocket.bind(null); // Bind to a random available port
                System.out.println("Server bound to port " + serverSocket.getLocalPort());
            }
            isRunning = true;

            // Main loop to accept clients
            while (isRunning) {
                try {
                    System.out.println("Waiting for client connections...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    String clientID = UUID.randomUUID().toString();
                    String clientIP = clientSocket.getInetAddress().getHostAddress();

                    System.out.println("New client connected: " + clientIP + " | Client ID: " + clientID);

                    // Add client to active clients
                    activeClients.put(clientID, clientIP);

                    // Create a new ClientHandler2 for the connected client
                    ClientHandler2 clientHandler = new ClientHandler2(clientSocket, this, messageHandler, authenticationSystem);
                    clientHandlers.add(clientHandler);

                    // Start the client handler thread
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                        e.printStackTrace(); // Debug stack trace
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start Server2: " + e.getMessage());
            e.printStackTrace(); // Debug stack trace
        } finally {
            shutdownServer();
        }
    }

 
	// Shuts down the server gracefully by closing the server socket and all client handlers.
    public void shutdownServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server2 socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing Server2 socket: " + e.getMessage());
            e.printStackTrace(); // Debug stack trace
        }

        // Close all client handlers
        for (ClientHandler2 handler : clientHandlers) {
            handler.closeConnection();
        }
        clientHandlers.clear();

        // Remove all active clients
        activeClients.clear();
        System.out.println("Server2 shutdown complete.");
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

    // Gets the server's non-loopback IPv4 address.
    private static String getServerIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;

                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            System.err.println("Couldn't get server IP address: " + e.getMessage());
            e.printStackTrace(); // Debug stack trace
            return "Unknown";
        }
    }

    // Main method to start the server.
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


