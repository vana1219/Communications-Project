package ServerApp.AuthenticationSystem;

import Common.User.User;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

/**
 * AuthenticationSystem manages user authentication and user-related operations.
 */
public class AuthenticationSystem {
    // Attributes
    private ConcurrentHashMap<Integer, User> userDB;
    private String userFilePath;

    // Constructor
    // *Initializes AuthenticationSystem and loads existing users from file*
    // INPUT: userFilePath (String)
    // OUTPUT: none
    public AuthenticationSystem(String userFilePath) {
        this.userFilePath = userFilePath;
        this.userDB = new ConcurrentHashMap<>();
        loadUsersFromFile(); // Load existing users from file
    }

    // *Registers a user in the system*
    // INPUT: user (User)
    // OUTPUT: true if registration is successful, false otherwise
    public boolean registerUser(User user) {
        if (user != null && !usernameExists(user.getUsername())) {
            userDB.put(user.getUserID(), user);
            saveUsersToFile(); // Save updated users to file
            return true;
        }
        return false;
    }

    // *Finds a user by username*
    // INPUT: username (String)
    // OUTPUT: User object or null if not found
    public User findUserByUsername(String username) {
        return userDB.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    // *Checks if a username already exists*
    // INPUT: username (String)
    // OUTPUT: true if username exists, false otherwise
    private boolean usernameExists(String username) {
        return userDB.values().stream().anyMatch(u -> u.getUsername().equals(username));
    }

    // *Validates user credentials during login*
    // INPUT: username (String), password (String)
    // OUTPUT: User object if login is successful, null otherwise
    public User validateCredentials(String username, String password) {
        for (User user : userDB.values()) {
            if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
                user.setOnline(true);
                saveUsersToFile();
                return user;
            }
        }
        return null;
    }

    // *Resets user password*
    // INPUT: userID (int), newPassword (String)
    // OUTPUT: true if reset is successful, false otherwise
    public boolean resetPassword(int userID, String newPassword) {
        User user = userDB.get(userID);
        if (user != null) {
            user.setPassword(newPassword);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // *Deletes a user from the system*
    // INPUT: userID (int)
    // OUTPUT: true if deletion is successful, false otherwise
    public boolean deleteUser(int userID) {
        if (userDB.containsKey(userID)) {
            userDB.remove(userID);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // *Logs a user out*
    // INPUT: userID (int)
    // OUTPUT: true if logout is successful, false otherwise
    public boolean logout(int userID) {
        User user = userDB.get(userID);
        if (user != null && user.isOnline()) {
            user.setOnline(false);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // *Finds a user by userID*
    // INPUT: userID (int)
    // OUTPUT: User object or null if not found
    public User findUser(int userID) {
        return userDB.get(userID);
    }

    // *Updates a user's information in the system*
    // INPUT: user (User)
    // OUTPUT: true if update is successful, false otherwise
    public boolean updateUser(User user) {
        if (user != null && userDB.containsKey(user.getUserID())) {
            userDB.put(user.getUserID(), user);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // *Retrieves all users in the system*
    // INPUT: none
    // OUTPUT: Collection of User objects
    public Collection<User> getAllUsers() {
        return userDB.values();
    }

    // *Retrieves the user database*
    // INPUT: none
    // OUTPUT: ConcurrentHashMap of userID to User objects
    public ConcurrentHashMap<Integer, User> getUserDB() {
        return userDB;
    }

    // *Saves all users to a file for persistent storage*
    // INPUT: none
    // OUTPUT: none
    private void saveUsersToFile() {
        synchronized (this) { // Ensure thread safety during save
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFilePath))) {
                oos.writeObject(userDB);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // *Loads users from file into memory*
    // INPUT: none
    // OUTPUT: none
    @SuppressWarnings("unchecked")
	private void loadUsersFromFile() {
        synchronized (this) { // Ensure thread safety during load
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFilePath))) {
                userDB = (ConcurrentHashMap<Integer, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                userDB = new ConcurrentHashMap<>(); // If file not found or error occurs, start with an empty HashMap
            }
        }
    }
}
