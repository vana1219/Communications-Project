package ServerApp.AuthenticationSystem;

import Common.Admin.Admin;
import Common.User.User;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.nio.file.*;

/**
 * AuthenticationSystem manages user authentication and user-related operations.
 */
public class AuthenticationSystem {
    // Attributes
    public static ConcurrentHashMap<Integer, User> userDB;
    private final String usersDirectory;

    // Constructor
    // Initializes AuthenticationSystem and loads existing users from files
    public AuthenticationSystem() {
        this.usersDirectory = "users"; // Directory to store user files
        this.userDB = new ConcurrentHashMap<>();
        createUsersDirectory();
        loadUsersFromFiles(); // Load existing users from files
    }

    // Creates the users directory if it doesn't exist
    private void createUsersDirectory() {
        File directory = new File(usersDirectory);
        if (!directory.exists()) {
            boolean created = directory.mkdir();
            if (created) {
                System.out.println("Users directory created.");
            } else {
                System.err.println("Failed to create Users directory.");
            }
        }
    }

    public boolean banUser(int userID) {
        User userToBan = userDB.get(userID);
        if (userToBan != null && !userToBan.isBanned()) {
            userToBan.setBanned(true);
            return true;
        }
        return false;
    }

    public boolean unbanUser(int userID) {
        User userToUnban = userDB.get(userID);
        if (userToUnban != null && userToUnban.isBanned()) {
            userToUnban.setBanned(false);
            return true;
        }
        return false;
    }

    public boolean isAdmin(int userID) {
        User user = userDB.get(userID);
        return user instanceof Admin;
    }

    
    // Registers a user in the system
    public boolean registerUser(User user) {
        if (user != null && !usernameExists(user.getUsername())) {
            userDB.put(user.getUserID(), user);
            saveUserToFile(user); // Save the user to its individual file
            return true;
        }
        return false;
    }

    // Finds a user by username
    public User findUserByUsername(String username) {
        return userDB.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }

    // Checks if a username already exists
    private boolean usernameExists(String username) {
        return userDB.values().stream().anyMatch(u -> u.getUsername().equals(username));
    }

    // Validates user credentials during login
    public User validateCredentials(String username, String password) {
        for (User user : userDB.values()) {
            if (user.getUsername().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
//            	 If the user is online already, reject double login
//            	if (user.isOnline()) {
//            		return null;
  //          	}
                if(!user.isBanned()) {
                    user.setOnline(true);
                    saveUserToFile(user);// Save updated user to file
                }
                return user;
            }
        }
        return null;
    }

    // Resets user password
    public boolean resetPassword(int userID, String newPassword) {
        User user = userDB.get(userID);
        if (user != null) {
            user.setPassword(newPassword);
            saveUserToFile(user); // Save updated user to file
            return true;
        }
        return false;
    }

    // Deletes a user from the system
    public boolean deleteUser(int userID) {
        if (userDB.containsKey(userID)) {
            userDB.remove(userID);
            deleteUserFile(userID); // Delete user's file
            return true;
        }
        return false;
    }

    // Logs a user out
    public boolean logout(int userID) {
        User user = userDB.get(userID);
        if (user != null && user.isOnline()) {
            user.setOnline(false);
            saveUserToFile(user); // Save updated user to file
            return true;
        }
        return false;
    }

    // Finds a user by userID
    public User findUser(int userID) {
        return userDB.get(userID);
    }

    // Updates a user's information in the system
    public boolean updateUser(User user) {
        if (user != null && userDB.containsKey(user.getUserID())) {
            userDB.put(user.getUserID(), user);
            saveUserToFile(user); // Save updated user to file
            return true;
        }
        return false;
    }

    // Retrieves all users in the system
    public Collection<User> getAllUsers() {
        return userDB.values();
    }

    // Retrieves the user database
    public ConcurrentHashMap<Integer, User> getUserDB() {
        return userDB;
    }

    // Saves a single user to its individual file
    private void saveUserToFile(User user) {
        synchronized (this) { // Ensure thread safety during save
            String fileName = usersDirectory + File.separator + user.getUserID(); // Filename is userID
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
                oos.writeObject(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Deletes a user's file
    private void deleteUserFile(int userID) {
        String fileName = usersDirectory + File.separator + userID;
        File file = new File(fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Failed to delete user file: " + fileName);
            }
        }
    }

    // Loads users from individual files into memory
    private void loadUsersFromFiles() {
        synchronized (this) {
            int maxUserId = 0;
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(usersDirectory))) {
                for (Path path : directoryStream) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                        User user = (User) ois.readObject();
                        userDB.put(user.getUserID(), user);
                        if (user.getUserID() > maxUserId) {
                            maxUserId = user.getUserID();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error loading user from file: " + path.getFileName());
                        e.printStackTrace();
                    }
                }
                // **Update userIdGenerator**
                User.setUserIdGenerator(maxUserId);
                System.out.println("Loaded " + userDB.size() + " users from files.");
            } catch (IOException e) {
                System.err.println("Error reading user files from directory: " + usersDirectory);
                e.printStackTrace();
            }
        }
    }
}
