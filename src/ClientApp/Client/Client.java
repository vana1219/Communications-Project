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
import Common.Messages.LoginResponse;
import Common.Messages.Notification;
import Common.Messages.SendChatBox;
import Common.Messages.SendChatLog;
import Common.Messages.SendMessage;
import Common.Messages.SendUserList;
import Common.Message.Message;
import Common.User.User;

import javax.swing.*;

public class Client {
    private boolean loggedIn = false;
    private BlockingQueue<MessageInterface> inboundRequestQueue;
    private BlockingQueue<MessageInterface> outboundResponseQueue;
    private User userData;
    private TreeSet<ChatBox> chatBoxList;
    private Gui gui;
    private ObjectOutputStream outObj = null;
    private ObjectInputStream inObj = null;
    private Socket socket = null;

    public Client() {
        chatBoxList = new TreeSet<>(Comparator.comparingInt(ChatBox::getChatBoxID));
        gui = new Gui(this); // Initialize GUI directly
        inboundRequestQueue = new LinkedBlockingQueue<>();
        outboundResponseQueue = new LinkedBlockingQueue<>();
    }

    private void handleServerResponses() {
        while (true) {
            MessageInterface response;
            try {
                response = inboundRequestQueue.take();
                if (response != null) {
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
        chatBoxList.remove(chatBox);
        chatBoxList.add(chatBox);
        // Update the GUI if necessary
        // gui.updateChatBox(chatBox);
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
        // Add the message to the appropriate chatbox
        // gui.addMessageToChatBox(message, chatBoxID);
    }

    // Handle SendChatLog messages
    private void handleReturnChatBoxLog(SendChatLog sendChatLog) {
        String chatBoxLog = sendChatLog.chatBoxLog();
        // Process chatBoxLog as needed
    }

    public TreeSet<ChatBox> getChatBoxList() {
        return chatBoxList;
    }

    private void receiveLoginResponse(LoginResponse loginResponse) {
        userData = loginResponse.user();
        chatBoxList.addAll(loginResponse.chatBoxList());
    }

    public void addMessage(MessageInterface message) {
        outboundResponseQueue.add(message);
    }

    public User getUserData() {
        return userData;
    }

    public void messageSender() {
        while (true) {
            MessageInterface message;
            try {
                message = outboundResponseQueue.take();
                outObj.writeObject(message);
                // outObj.flush(); ???
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void messageReceiver() {
        while (true) {
            try {
                inboundRequestQueue.add((MessageInterface) inObj.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
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

            senderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.messageSender();
                }
            });
            receiverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.messageReceiver();
                }
            });
            senderThread.start();
            receiverThread.start();

            while (!client.loggedIn) {
                client.addMessage(client.gui.login());
                MessageInterface response = client.inboundRequestQueue.take();
                if (response.getType() == MessageType.LOGIN_RESPONSE) {
                    client.receiveLoginResponse((LoginResponse) response);

                    if (client.userData != null) {
                        client.loggedIn = true;
                        System.out.println("Logged in.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            client.gui.showMain();
            // Handle server responses
            client.handleServerResponses();

        } catch (IOException | InterruptedException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}


