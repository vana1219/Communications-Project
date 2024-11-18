package ServerApp.Admin;

import ServerApp.User.User;
import ServerApp.AuthenticationSystem.AuthenticationSystem;
import ServerApp.MessageHandler.MessageHandler;

public class Admin extends User {

    // Attributes
    private MessageHandler messageHandler;
    private AuthenticationSystem authenticationSystem;

    // Constructor
    // INPUT: username (String), password (String), messageHandler (MessageHandler), authenticationSystem (AuthenticationSystem)
    public Admin(String username, String password, MessageHandler messageHandler, AuthenticationSystem authenticationSystem) {
        super(username, password);
        this.messageHandler = messageHandler;
        this.authenticationSystem = authenticationSystem;
    }

    // Bans a user from the system
    // INPUT: userID (int)
    // OUTPUT: true if ban was successful, false otherwise
    public boolean banUser(int userID) {
        User user = authenticationSystem.findUser(userID);
        if (user != null && !user.isBanned()) {
            user.setBanned(true);
            authenticationSystem.updateUser(user);
            return true;
        }
        return false;
    }

    // Unbans a user from the system
    // INPUT: userID (int)
    // OUTPUT: true if unban was successful, false otherwise
    public boolean unbanUser(int userID) {
        User user = authenticationSystem.findUser(userID);
        if (user != null && user.isBanned()) {
            user.setBanned(false);
            authenticationSystem.updateUser(user);
            return true;
        }
        return false;
    }

    // Adds a user to the system
    // INPUT: user (User)
    // OUTPUT: true if user added successfully, false otherwise
    public boolean addUser(User user) {
        return authenticationSystem.registerUser(user);
    }

    // Deletes a user from the system
    // INPUT: userID (int)
    // OUTPUT: true if user deleted successfully, false otherwise
    public boolean deleteUser(int userID) {
        return authenticationSystem.deleteUser(userID);
    }

    // Resets a user's password
    // INPUT: userID (int), newPassword (String)
    // OUTPUT: true if password reset successfully, false otherwise
    public boolean resetUserPassword(int userID, String newPassword) {
        return authenticationSystem.resetPassword(userID, newPassword);
    }

    // Sends a system-wide message to all users
    // INPUT: content (String)
    // OUTPUT: none
    public void sendSystemMessage(String content) {
        for (User user : authenticationSystem.getAllUsers()) {
            if (user.isOnline()) {
                messageHandler.sendMessageToUser(user.getUserID(), content);
            }
        }
    }

    // Hides a specific message in a chatbox using MessageHandler
    // INPUT: chatBoxID (int), messageID (int)
    // OUTPUT: true if message was hidden successfully, false otherwise
    public boolean hideChatMessage(int chatBoxID, int messageID) {
        return messageHandler.hideMessage(chatBoxID, messageID);
    }

    // Hides a specific chatbox using MessageHandler
    // INPUT: chatBoxID (int)
    // OUTPUT: true if chatbox was hidden successfully, false otherwise
    public boolean hideChatBox(int chatBoxID) {
        return messageHandler.hideChatBox(chatBoxID);
    }
}

