package Common.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger userIdGenerator = new AtomicInteger(0);

    private final int userID;
    private String username;
    private String password;
    private transient boolean isOnline;
    private boolean isBanned;

    // Constructor
    public User(String username, String password) {
        this.userID = userIdGenerator.incrementAndGet();
        this.username = username;
        this.password = password;
        this.isOnline = false;
        this.isBanned = false;
    }
    
    public static void setUserIdGenerator(int value) {
        userIdGenerator.set(value);
    }

    // Getters and setters
    public int getUserID() {
        return userID;
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

    public String toString(){
        return username;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    @Override
    public boolean equals(Object user) {
        return user instanceof User && userID == ((User) user).userID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(userID);
    }



}

