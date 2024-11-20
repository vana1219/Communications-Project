package ServerApp.ClientHandler2;

import ServerApp.Server2.Server2;
import ServerApp.MessageHandler.MessageHandler;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.ChatBox.ChatBox;
import ServerApp.User.User;
import Common.MessageInterface;
import Common.Messages.*;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientHandler2 implements Runnable {

    // Attributes
    private Socket clientSocket;
    private Server2 server;
    private MessageHandler messageHandler;
    private AuthenticationSystem authenticationSystem;
    private User user;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean isRunning;

    // Constructor
    public ClientHandler2(Socket clientSocket, Server2 server, MessageHandler messageHandler, AuthenticationSystem authenticationSystem) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.messageHandler = messageHandler;
        this.authenticationSystem = authenticationSystem;
        this.isRunning = true;
        try {
            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            this.input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    // Handles client communication and requests
    @Override
    public void run() {
        try {
            // Authentication Loop
            while (isRunning && user == null) {
                MessageInterface request = (MessageInterface) input.readObject();
                handleMessage(request);
            }

            // Main communication loop
            while (isRunning) {
                MessageInterface request = (MessageInterface) input.readObject();
                handleMessage(request);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error with client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // Handles different message types
    private void handleMessage(MessageInterface message) {
        switch (message.getType()) {
            case LOGIN -> handleLogin((Login) message);
            case CREATE_USER -> handleCreateUser((CreateUser) message);
            case SEND_MESSAGE -> handleSendMessage((SendMessage) message);
            case LOGOUT -> handleLogout();
            default -> sendNotification("Unknown message type received.");
        }
    }

    // Handle Login
    private void handleLogin(Login login) {
    	//
    }

    // Handle CreateUser
    private void handleCreateUser(CreateUser createUser) {
    	//
    }

    // Handle SendMessage
    private void handleSendMessage(SendMessage sendMessage) {
        boolean success = messageHandler.sendMessage(sendMessage.chatBoxID(), sendMessage.message());
        if (!success) {
            sendNotification("Failed to send message.");
        }
    }

    // Handle Logout
    private void handleLogout() {
        authenticationSystem.logout(user.getUserID());
        sendNotification("Logout successful.");
        closeConnection();
    }

    // Send a message to the client
    private void sendMessage(MessageInterface message) {
        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }

    // Send a notification to the client
    private void sendNotification(String text) {
        Notification notification = new Notification(text);
        sendMessage(notification);
    }

    // Closes the client connection and cleans up resources
    public void closeConnection() {
        isRunning = false;
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        } finally {
            server.removeClientHandler(this);
            if (user != null) {
                authenticationSystem.logout(user.getUserID());
            }
        }
    }

    // Retrieves the client socket
    public Socket getClientSocket() {
        return clientSocket;
    }

    // Retrieves the user associated with this client
    public User getUser() {
        return user;
    }

    // Sets the user associated with this client
    public void setUser(User user) {
        this.user = user;
    }

	public void sendChatBoxUpdate(ChatBox chatBox) {
		// TODO Auto-generated method stub
		
	}
}

