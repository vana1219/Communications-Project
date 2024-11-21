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

        try {
            // Get server IP and port from the user
            System.out.print("Enter server IP address: ");
            String serverIP = scanner.nextLine();

            System.out.print("Enter server port number: ");
            int port = Integer.parseInt(scanner.nextLine());

            // Connect to the server
            Socket socket = new Socket(serverIP, port);
            System.out.println("Connected to the server.");

            // Set up object streams
            ObjectOutputStream outObj = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inObj = new ObjectInputStream(socket.getInputStream());

            // Get username and password from the user
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Create and send the login message
            Login loginMessage = new Login(username, password);
            outObj.writeObject(loginMessage);
            outObj.flush();

         // Read and ignore the server's reply
            try {
                @SuppressWarnings("unused")
				Object response = inObj.readObject();
                // Ignore the response
            } catch (ClassNotFoundException e) {
                // Handle exception if necessary
            }


            // Close resources
            outObj.close();
            inObj.close();
            socket.close();
            scanner.close();

            System.out.println("Login message sent to the server.");

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
