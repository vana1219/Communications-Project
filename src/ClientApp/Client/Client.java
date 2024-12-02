package ClientApp.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Import existing classes from your Common package
import ClientApp.Gui.Gui;
import ClientApp.Gui.ConnectionInfo;
import Common.ChatBox.ChatBox;
import Common.MessageInterface;
import Common.MessageType;
import Common.Messages.*;
import Common.Message.Message;
import Common.User.User;

import javax.swing.*;

public class Client {
    private boolean loggedIn = false;
    private final BlockingQueue<MessageInterface> inboundRequestQueue;
    private final BlockingQueue<MessageInterface> outboundResponseQueue;
    private User userData;
    private final Gui gui;
    private ObjectOutputStream outObj = null;
    private ObjectInputStream inObj = null;
    private Socket socket = null;

    public Client() {
        gui = new Gui(this); // Initialize GUI directly
        inboundRequestQueue = new LinkedBlockingQueue<>();
        outboundResponseQueue = new LinkedBlockingQueue<>();
    }


    private void handleServerResponses() {
        MessageInterface response;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                response = inboundRequestQueue.take();
                switch (response.getType()) {
                    case MessageType.LOGIN_RESPONSE:
                        receiveLoginResponse((LoginResponse) response);
                        break;
                    case MessageType.NOTIFICATION:
                        handleNotification((Notification) response);
                        break;
                    case MessageType.RETURN_CHATBOX:
                        handleReturnChatBox((SendChatBox) response);
                        break;
                    case MessageType.RETURN_USER_LIST:
                        handleReturnUserList((SendUserList) response);
                        break;
                    case MessageType.SEND_MESSAGE:
                        handleSendMessage((SendMessage) response);
                        break;
                    case MessageType.RETURN_CHATBOX_LOG:
                        handleReturnChatBoxLog((SendChatLog) response);
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException e) {
                System.err.println("Error handling server response: " + e.getMessage());
            }
        }
    }

    // Add methods to handle the server responses

    // Handle Notification messages
    private void handleNotification(Notification notification) {
        String text = notification.text();
        JOptionPane.showMessageDialog(null, text, "Notification", JOptionPane.INFORMATION_MESSAGE);
    }

    // Handle SendChatBox messages
    private void handleReturnChatBox(SendChatBox sendChatBox) {
        ChatBox chatBox = sendChatBox.chatBox();
        if (gui.getChatBox().equals(chatBox)) {
            gui.clearMessages();
            gui.addAllMessages(chatBox);
        } else if(!gui.containsChatBox(chatBox)) {
            gui.addChatBox(chatBox);
        }
    }

    // Handle SendUserList messages
    private void handleReturnUserList(SendUserList sendUserList) {
        List<User> userList = sendUserList.userList();
        // Process user list as needed
    }

    // Handle SendMessage messages
    private void handleSendMessage(SendMessage sendMessage) {
        Message message = sendMessage.message();
        int chatBoxID = sendMessage.chatBoxID();
        if (gui.getChatBox().getChatBoxID() == chatBoxID) {
            gui.getChatBox().addMessage(message);
            gui.addMessage(message);
        } else {
            gui.getChatBox(chatBoxID).addMessage(message);
        }
        // Add the message to the appropriate chatbox
        // gui.addMessageToChatBox(message, chatBoxID);
    }

    // Handle SendChatLog messages
    private void handleReturnChatBoxLog(SendChatLog sendChatLog) {
        String chatBoxLog = sendChatLog.chatBoxLog();
        // Process chatBoxLog as needed
    }


    private void receiveLoginResponse(LoginResponse loginResponse) {
        userData = loginResponse.user();

        if (loginResponse.chatBoxList() != null && !loginResponse.chatBoxList().isEmpty()) {
            SwingUtilities.invokeLater(() -> gui.addAllChatBoxes(loginResponse.chatBoxList()));
        }
    }


    public void queueMessage(MessageInterface message) {
        outboundResponseQueue.add(message);
    }


    public User getUserData() {
        return userData;

    }


    public void messageSender() {
        while (!Thread.currentThread().isInterrupted()) {
            MessageInterface message;
            try {
                message = outboundResponseQueue.take();
                outObj.reset();
                outObj.writeObject(message);

            } catch (IOException | InterruptedException e) {
//                throw new RuntimeException(e);
            }
        }
    }


    public void messageReceiver() {
        while (!Thread.interrupted()) {
            try {
                inboundRequestQueue.add((MessageInterface) inObj.readObject());
            } catch (IOException | ClassNotFoundException e) {
//                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        Thread senderThread = null;
        Thread receiverThread = null;

        try {
            // Get server IP and port from the GUI
            ConnectionInfo connectionInfo = client.gui.getConnectionInfo();
            String serverIP = connectionInfo.getServerIP();
            int port = connectionInfo.getPort();

            // Connect to the server
            client.socket = new Socket(serverIP, port);
            System.out.println("Connected to the server.");

            // Set up object streams
            client.outObj = new ObjectOutputStream(client.socket.getOutputStream());
            client.inObj = new ObjectInputStream(client.socket.getInputStream());

            senderThread = new Thread(null, client::messageSender, "SenderThread");
            receiverThread = new Thread(null, client::messageReceiver, "ReceiverThread");
            senderThread.start();
            receiverThread.start();

            while (!client.loggedIn) {
                client.queueMessage(client.gui.login());
                MessageInterface response = client.inboundRequestQueue.take();
                if (response.getType() == MessageType.LOGIN_RESPONSE) {
                    client.receiveLoginResponse((LoginResponse) response);

                    if (client.userData != null) {

                        client.loggedIn = true;
                        System.out.println("Logged in.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Failed",
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            Thread.sleep(500);
            client.queueMessage(new AskChatBox(client.gui.getChatBox().getChatBoxID()));
            SendChatBox response = (SendChatBox) client.inboundRequestQueue.take();
            client.handleReturnChatBox(response);


            client.gui.showMain();
            // Handle server responses
            client.handleServerResponses();

        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error: " + e.getMessage());

        } finally {
            // Close resources
            try {
                if (senderThread != null) {
                    senderThread.interrupt();
                }
                if (receiverThread != null) {
                    receiverThread.interrupt();
                }
                if (client.outObj != null) {
                    client.outObj.close();
                }
                if (client.inObj != null) {
                    client.inObj.close();
                }
                if (client.socket != null) {
                    client.socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
