package src.ServerApp.User;

public class User {
	// Attributes
    private String userID;
    private String username;
    private String password;
    private boolean isOnline;
    private boolean isBanned;

    // Constructor
    public User(String userID, String username, String password) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.isOnline = false;
        this.isBanned = false;
    }

    // Getters and setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    // Methods
    public boolean checkIfBanned() {
        return isBanned;
    }

    public boolean checkIfOnline() {
        return isOnline;
    }
}