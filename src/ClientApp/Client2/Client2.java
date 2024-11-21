package ClientApp.Client2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

// Import existing classes from your Common package
import Common.MessageInterface;
import Common.MessageType;
import Common.Messages.Login;

public class Client2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ObjectOutputStream outObj = null;
        ObjectInputStream inObj = null;
        Socket socket = null;


        try {
            // Get server IP and port from the user
            System.out.print("Enter server IP address: ");
            String serverIP = scanner.nextLine();

            System.out.print("Enter server port number: ");
            int port = Integer.parseInt(scanner.nextLine());

            // Connect to the server
            socket = new Socket(serverIP, port);
            System.out.println("Connected to the server.");

            // Set up object streams
            outObj = new ObjectOutputStream(socket.getOutputStream());
            inObj = new ObjectInputStream(socket.getInputStream());

            // Get username and password from the user
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Create and send the login message
            Login loginMessage = new Login(username, password);
            outObj.writeObject(loginMessage);
            outObj.flush();
            
         // Handle server responses
            handleServerResponses(inObj);

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } finally {
            // Close resources
            try {
                if (outObj != null) outObj.close();
                if (inObj != null) inObj.close();
                if (socket != null) socket.close();
                scanner.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private static void handleServerResponses(ObjectInputStream inObj) {
        while (true) {
            try {
                Object response = inObj.readObject();
             // Process the response if needed
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
                break;
            }
        }
    }
}
         