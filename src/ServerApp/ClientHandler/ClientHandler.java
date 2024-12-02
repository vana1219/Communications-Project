package ServerApp.ClientHandler;

import ServerApp.Server.Server;
import ServerApp.MessageHandler.MessageHandler;
import Common.Admin.Admin;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import Common.ChatBox.ChatBox;
import Common.Message.Message;
import Common.User.User;
import Common.MessageInterface;
import Common.Messages.*;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class ClientHandler implements Runnable {

    // Attributes
    private final Socket clientSocket;
    private final Server server;
    private final MessageHandler messageHandler;
    private final AuthenticationSystem authenticationSystem;
    private User user;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean isRunning;

    // Constructor
    public ClientHandler(Socket clientSocket, Server server, MessageHandler messageHandler,
            AuthenticationSystem authenticationSystem) {
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
            case BAN_USER -> handleBanUser((BanUser) message);
            case UNBAN_USER -> handleUnbanUser((UnbanUser) message);
            case REQUEST_USER_LIST -> handleRequestUserList();
            case REQUEST_CHATBOX -> handleRequestChatBox((AskChatBox) message);
            case CREATE_CHATBOX -> handleCreateChatBox((CreateChat) message);
            case REQUEST_CHATBOX_LIST -> handleRequestChatBoxList();
            case VIEW_CHATBOX_LOG -> handleViewChatBoxLog((AskChatLog) message);
            default -> sendNotification("Unknown message type received.");
        }
    }
    
    private void handleRequestChatBoxList() {
        if (!authenticationSystem.isAdmin(user.getUserID())) {
            sendNotification("Access denied. Admin privileges required to view chatbox list.");
            return;
        }

        Collection<ChatBox> chatBoxes = server.getChatBoxes().values();
        List<ChatBox> chatBoxList = new ArrayList<>(chatBoxes);
        SendChatBoxList response = new SendChatBoxList(chatBoxList);
        sendMessage(response);
    }
    
    private void handleViewChatBoxLog(AskChatLog askChatLog) {
        if (!authenticationSystem.isAdmin(user.getUserID())) {
            sendNotification("Access denied. Admin privileges required to view chat logs.");
            return;
        }

        int chatBoxID = askChatLog.chatBoxID();
        ChatBox chatBox = messageHandler.getChatBox(chatBoxID);

        if (chatBox != null) {
            StringBuilder chatLogBuilder = new StringBuilder();
            for (Message message : chatBox.getMessages()) {
                chatLogBuilder.append(message.getTimestamp()).append(" - ")
                              .append(message.getSenderID()).append(": ")
                              .append(message.toString()).append("\n");
            }
            String chatLog = chatLogBuilder.toString();
            SendChatLog response = new SendChatLog(chatLog);
            sendMessage(response);
        } else {
            sendNotification("ChatBox not found.");
        }
    }
    
    public boolean banUser(int userID) {
        User userToBan = AuthenticationSystem.userDB.get(userID);
        if (userToBan != null && !userToBan.isBanned()) {
            userToBan.setBanned(true);
            return true;
        }
        return false;
    }

    public boolean unbanUser(int userID) {
        User userToUnban = AuthenticationSystem.userDB.get(userID);
        if (userToUnban != null && userToUnban.isBanned()) {
            userToUnban.setBanned(false);
            return true;
        }
        return false;
    }

    public boolean isAdmin(int userID) {
        User user = AuthenticationSystem.userDB.get(userID);
        return user instanceof Admin;
    }

	// Handle Login
	private void handleLogin(Login login) {
		String username = login.username();
		String password = login.password();

		// Validate credentials using AuthenticationSystem
		User authenticatedUser = authenticationSystem.validateCredentials(username, password);

		if (authenticatedUser != null) {
			// Successful login
			this.user = authenticatedUser;
			System.out.println("User logged in: " + user.getUsername());

			// Retrieve all ChatBoxes the user is part of
			List<ChatBox> userChatBoxes = server.getChatBoxes().values().stream()
					.filter(chatBox -> chatBox.getParticipants().contains(user)).map(ChatBox::getEmpty).toList();

			// Create and send LoginResponse
			LoginResponse loginResponse = new LoginResponse(user, userChatBoxes);
			sendMessage(loginResponse);
		} else {
			// Failed login
			System.out.println("Failed login attempt for username: " + username);

			// Create and send LoginResponse indicating failure
			LoginResponse loginResponse = new LoginResponse(null, null);
			sendMessage(loginResponse);
		}
	}

	
	// Handle CreateUser
    private void handleCreateUser(CreateUser createUser) {
        // Step 1: Check if the requesting user is an admin
        if (!(user instanceof Admin adminUser)) {
            System.out.println("User " + (user != null ? user.getUsername() : "Unknown") + " attempted to create a user without admin privileges.");

            // Send a Notification indicating denial
            Notification response = new Notification("Access denied. Admin privileges required to create a new user.");
            sendMessage(response);
            return;
        }

        // Step 2: Proceed with user registration
        String newUsername = createUser.username();
        String newPassword = createUser.password();
        boolean isAdmin = createUser.isAdmin();

        // Create a new User or Admin instance based on isAdmin flag
        User newUser;
        if (isAdmin) {
            newUser = new Admin(newUsername, newPassword, messageHandler, authenticationSystem);
        } else {
            newUser = new User(newUsername, newPassword);
        }

        // Use Admin's addUser method
        boolean registrationSuccess = adminUser.addUser(newUser);

        if (registrationSuccess) {
            System.out.println("Admin " + adminUser.getUsername() + " successfully created user: " + newUsername);

            // Send a Notification indicating success
            Notification response = new Notification("User created successfully.");
            sendMessage(response);
        } else {
            System.out.println("Admin " + adminUser.getUsername() + " failed to create user: " + newUsername);

            // Send a Notification indicating failure
            Notification response = new Notification("Failed to create user. Username may already exist.");
            sendMessage(response);
        }
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
		sendMessage(new LogoutResponse());

        closeConnection();
	}

	   // Handle BanUser
	private void handleBanUser(BanUser banUser) {
	    int userIDToBan = banUser.userID();

	    boolean success = authenticationSystem.banUser(userIDToBan);
	    if (success) {
	        System.out.println("Admin " + user.getUsername() + " banned user with ID: " + userIDToBan);
	        sendNotification("User banned successfully.");
	    } else {
	        System.out.println("Admin " + user.getUsername() + " failed to ban user with ID: " + userIDToBan);
	        sendNotification("Failed to ban user. User may not exist or is already banned.");
	    }
	}

    // Handle UnbanUser
	private void handleUnbanUser(UnbanUser unbanUser) {
	    int userIDToUnban = unbanUser.userID();

	    boolean success = authenticationSystem.unbanUser(userIDToUnban);
	    if (success) {
	        System.out.println("Admin " + user.getUsername() + " unbanned user with ID: " + userIDToUnban);
	        sendNotification("User unbanned successfully.");
	    } else {
	        System.out.println("Admin " + user.getUsername() + " failed to unban user with ID: " + userIDToUnban);
	        sendNotification("Failed to unban user. User may not exist or is not banned.");
	    }
	}

    // Handle RequestUserList
    private void handleRequestUserList() {
        Collection<User> users = authenticationSystem.getAllUsers();
        List<User> userList = new ArrayList<>(users);
        SendUserList response = new SendUserList(userList);
        sendMessage(response);
    }

    // Handle RequestChatBox
    private void handleRequestChatBox(AskChatBox askChatBox) {
        int chatBoxID;
        try {
            chatBoxID = askChatBox.chatBoxID();
        } catch (NumberFormatException e) {
            sendNotification("Invalid chatBox ID provided.");
            return;
        }

        ChatBox chatBox = messageHandler.getChatBox(chatBoxID);
        if (chatBox != null) {
            if (chatBox.getParticipants().contains(user)) {
                SendChatBox response = new SendChatBox(chatBox);
                sendMessage(response);
            } else {
                sendNotification("Access denied. You are not a participant of this chatbox.");
            }
        } else {
            sendNotification("ChatBox not found.");
        }
    }

    // Handle CreateChatBox
    private void handleCreateChatBox(CreateChat createChat) {
        List<User> participants = createChat.participants();

        List<User> validatedParticipants = new ArrayList<>();
        for (User userSent : participants) {
            // Find the user in the authentication system by username
            User serverUser = authenticationSystem.findUserByUsername(userSent.getUsername());
            if (serverUser != null) {
                validatedParticipants.add(serverUser);
            } else {
                sendNotification("User not found: " + userSent.getUsername());
                return;
            }
        }

        // Include the requesting user if not already included
        if (!validatedParticipants.contains(user)) {
            validatedParticipants.add(user);
        }

        ChatBox chatBox = messageHandler.createChatBox(validatedParticipants, createChat.name());

        if (chatBox != null) {
            SendChatBox response = new SendChatBox(chatBox);
            sendMessage(response);
        } else {
            sendNotification("Failed to create chatbox.");
        }
    }
	
	// Send a message to the client
	private void sendMessage(MessageInterface message) {
		try {
			output.reset();
			output.writeObject(message);
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
			if (input != null)
				input.close();
			if (output != null)
				output.close();
			if (clientSocket != null && !clientSocket.isClosed())
				clientSocket.close();
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

    // Sends an updated chatbox to the client
    public void sendChatBoxUpdate(ChatBox chatBox) {
        SendChatBox response = new SendChatBox(chatBox);
        sendMessage(response);
    }
}
