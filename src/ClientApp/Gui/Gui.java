package ClientApp.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ClientApp.Client.Client;
import Common.Messages.SendMessage;
import ServerApp.ChatBox.ChatBox;
import ServerApp.Message.Message;
import ServerApp.User.User;

public class Gui {
    volatile boolean loggedIn = false;
    private ChatBox chatBox = new ChatBox();
    private User user = new User("bob", "bobpass");
    private Client client;
    private JFrame frame;
    private JList<String> chatArea;
    private JTextArea messageField;
    private JButton sendButton;
    private JPanel messagePane;
    private JScrollPane scrollPane;
    private JScrollPane messageScrollPane;
    private DefaultListModel<String> model;
    private JPanel loginPanel;

    public Gui(Client client) {
        this.client = client;
        client.start();
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);  // Center the window
        login();

    }

    public void viewChat(){
        frame.setSize(600, 500);
        // Create the chat area (used to display messages)
        model = new DefaultListModel<>();
        chatArea = new JList<>(model);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
//        chatArea.setEditable(false);  // Messages should not be editable

        // Create the scroll pane for the chat area
        scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Create the message input field

        messageField = new JTextArea();
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageScrollPane = new JScrollPane(messageField);
        messageScrollPane.setPreferredSize(new Dimension(500, 60));
        messagePane = new JPanel();
        messagePane.add(messageScrollPane);
        // Create the send button
        sendButton = new JButton("Send");
        messagePane.add(sendButton);
        frame.setResizable(false);


        // Action listener for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Create the layout and add components
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(new JPanel(), BorderLayout.SOUTH);
        panel.add(messagePane, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
    }

    // Method to send a message
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(new SendMessage(new Message(user.getUserID(), message),chatBox.getChatBoxID()));
            messageField.setText("");// Clear the input field
        }
    }

    private void login(){
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new FlowLayout());
        usernamePanel.add(new JLabel("Username:"));
        JTextField userNameField = new JTextField();
        userNameField.setPreferredSize(new Dimension(150, userNameField.getPreferredSize().height));
        usernamePanel.add(userNameField);
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new FlowLayout());
        passwordPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(150, passwordField.getPreferredSize().height));
        passwordPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.login(userNameField.getText(), String.valueOf(passwordField.getPassword()));
                loginPanel.setVisible(false);
                viewChat();
            }
        });

        loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.add(usernamePanel);
        loginPanel.add(passwordPanel);
        loginPanel.add(loginButton);

        frame.getContentPane().add(loginPanel);
        frame.setSize(300,200);
//        frame.setVisible(true);
    }

    private void addMessage(String message) {
        model.addElement("<html><b>You: </b>" + message.replace("\n", "<br><plaintext>     </plaintext>") + "<br></html>");
    }

    // Method to display the chat window
    public void display() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread for thread-safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Gui gui = null;
                try {
                    gui = new Gui(new Client(InetAddress.getByName("192.168.1.45"), 1234));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }

                gui.display();
            }
        });
    }
}
