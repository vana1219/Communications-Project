package ServerApp.Admin;
import ServerApp.User.User; 

// Admin INHERENTS from User
public class Admin extends User {

    // Constructor
    public Admin(String username, String password) {
        super(username, password);
    }

    // Methods 
    // NOTE - METHOD NOT COMPLETE
    public boolean banUser(String userID) {
    	// Placeholder for method implementation
        return false;
    }

    // NOTE - METHOD NOT COMPLETE
    public boolean unbanUser(String userID) {
        // Placeholder for method implementation
        return false;
    }
    
    // NOTE - METHOD NOT COMPLETE
    public boolean addUser(User user) {
        // Placeholder for method implementation
        return false;
    }
    
    // NOTE - METHOD NOT COMPLETE
    public boolean deleteUser(String userID) {
        // Placeholder for method implementation
        return false;
    }

    // NOTE - METHOD NOT COMPLETE
    public boolean resetUserPassword(String userID, String newPassword) {
        // Placeholder for method implementation
        return false;
    }

    // NOTE - METHOD NOT COMPLETE
    public void sendSystemMessage(String content) {
        // Placeholder for method implementation
    }

    // NOTE - METHOD NOT COMPLETE
    public boolean hideChatMessage(String chatBoxID, String messageID) {
        // Placeholder for method implementation
        return false;
    }

    // NOTE - METHOD NOT COMPLETE
    public boolean hideChatBox(String chatID) {
        // Placeholder for method implementation
        return false;
    }

    // NOTE - METHOD NOT COMPLETE
    public String getChatBoxLog(String chatBoxID) {
        // Placeholder for method implementation
        return "";
    }
}