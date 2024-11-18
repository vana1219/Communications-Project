package ServerApp.AuthenticationSystem;

import ServerApp.User.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class AuthenticationSystem {
    // Attributes
    private Map<Integer, User> userDB;
    private String userFilePath;

    // Constructor
    // Creates a new instance of AuthenticationSystem
    // INPUT: userFilePath (String), userDB (Map<Integer, User>)
    public AuthenticationSystem(String userFilePath, Map<Integer, User> userDB) {
        this.userFilePath = userFilePath;
        this.userDB = userDB;
    }

    // Loads users from a file containing serialized objects
    // OUTPUT: Map of userID to User objects
    private Map<Integer, User> loadUsersFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userFilePath))) {
            return (Map<Integer, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Saves all users to a file
    // OUTPUT: true if save is successful, false otherwise
    private boolean saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFilePath))) {
            oos.writeObject(userDB);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Registers a new user to the system
    // INPUT: user (User)
    // OUTPUT: true if registration is successful, false otherwise
    public boolean registerUser(User user) {
        if (!userDB.containsKey(user.getUserID())) {
            userDB.put(user.getUserID(), user);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // Validates user credentials during login
    // INPUT: username (String), password (String)
    // OUTPUT: User object if login is successful, null otherwise
    public User validateCredentials(String username, String password) {
        for (User user : userDB.values()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                user.setOnline(true);
                saveUsersToFile();
                return user;
            }
        }
        return null;
    }

    // Resets user password
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

    // Deletes a user from the system
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

    // Logs a user out
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

    // Finds a user by userID
    // INPUT: userID (int)
    // OUTPUT: User object or null if not found
    public User findUser(int userID) {
        return userDB.get(userID);
    }

    // Updates a user's information in the system
    // INPUT: user (User)
    // OUTPUT: true if update is successful, false otherwise
    public boolean updateUser(User user) {
        if (userDB.containsKey(user.getUserID())) {
            userDB.put(user.getUserID(), user);
            saveUsersToFile();
            return true;
        }
        return false;
    }

    // Retrieves all users in the system
    // OUTPUT: Collection of User objects
    public Collection<User> getAllUsers() {
        return userDB.values();
    }
}