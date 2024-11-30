package ClientApp.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import ClientApp.Client.Client;
import Common.Messages.Login;
import Common.Messages.SendMessage;
import Common.ChatBox.ChatBox;
import Common.Message.Message;
import Common.User.User;


public class Gui {
    volatile boolean loggedIn = false;
    private final Client client;
    private final JFrame frame;
    private final LoginWindow loginWindow;
    private final MainWindow mainWindow;
    private final ConnectionWindow connectionWindow; // Added connection window
    private final TreeListModel<ChatBox> treeListModel;
  
    public Gui(Client client) {
        this.client = client;
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);// Center the window
        treeListModel = new TreeListModel<>(Comparator.comparingInt(ChatBox::getChatBoxID));

        loginWindow = new LoginWindow();
        mainWindow = new MainWindow();
        connectionWindow = new ConnectionWindow();// Initialize connection window
    }

    // Method to get connection info from the user
    public ConnectionInfo getConnectionInfo() {
        return connectionWindow.getConnectionInfo();
    }

    public void showMain() {
        frame.getContentPane().add(mainWindow.panel);

        if(!treeListModel.isEmpty()) {
            mainWindow.chatBox = treeListModel.getElementAt(0);
            addAllMessages(mainWindow.chatBox);
        }
        frame.setLocationRelativeTo(null);
        frame.setSize(new Dimension(1000, 600));;
        show();
    }

    public void addChatBox(ChatBox chatBox) {
        if(!treeListModel.isEmpty()) {
            treeListModel.remove(chatBox);
        }
        treeListModel.add(chatBox);
    }
    public void addAllChatBoxes(Collection<? extends ChatBox> chatBoxes) {
        treeListModel.addAll(chatBoxes);
    }

    // Send a message to the server.
    private void sendMessage() {
        mainWindow.sendMessage();
    }

    public Login login() {
        frame.setVisible(false);
        return loginWindow.getLogin();
    }

  
    // Add a message to the display.
    public void addMessage(Message message) {
        User user = idToUser(message.getSenderID(), mainWindow.chatBox);
        String username;
        if (user == null) {
            username = String.valueOf(message.getSenderID());
        }else{
            username = user.getUsername();
        }
        mainWindow.chatModel.addElement("<html><b>"+username+": </b>" +
                         message.toString().replace("\n", "<br><plaintext>     </plaintext>") +
                         "<br></html>");
    }



    public User idToUser(int userId, ChatBox chatBox) {
        for(var i:chatBox.getParticipantsList()){
            if(i.getUserID() == userId){
                return i;
            }
        }
        return null;
    }

    public void show() {
        frame.setVisible(true);
    }

    private void addAllMessages(ChatBox chatBox) {
       for(var i:chatBox.getMessages()){
           addMessage(i);
       }
    }
  
  
//    public static void main(String[] args) {
//        // Run the GUI on the Event Dispatch Thread for thread-safety
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                Gui gui = null;
//                gui = new Gui(new Client());
//
//                gui.display();
//            }
//        });
//    }


    private static class LoginWindow {
        private final JFrame frame;
        private final JTextField userNameField;
        private final JPasswordField passwordField;
        private CountDownLatch latch;
        private final Login[] login;

        public LoginWindow() {
            frame = new JFrame("Login");
            login = new Login[1];
            JPanel usernamePanel = new JPanel();
            usernamePanel.setLayout(new FlowLayout());
            usernamePanel.add(new JLabel("Username:"));
            userNameField = new JTextField();
            userNameField.setPreferredSize(new Dimension(150, userNameField.getPreferredSize().height));
            usernamePanel.add(userNameField);
            JPanel passwordPanel = new JPanel();
            passwordPanel.setLayout(new FlowLayout());
            passwordPanel.add(new JLabel("Password:"));
            passwordField = new JPasswordField();
            passwordField.setPreferredSize(new Dimension(150, passwordField.getPreferredSize().height));
            passwordPanel.add(passwordField);
            JButton loginButton = getLoginButton();

            JPanel loginPanel = new JPanel();
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
            loginPanel.add(usernamePanel);
            loginPanel.add(passwordPanel);
            loginPanel.add(loginButton);

            frame.getContentPane().add(loginPanel);
            frame.setAlwaysOnTop(true);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setResizable(false);
        }

        private JButton getLoginButton() {
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    login[0] = new Login(userNameField.getText(), String.valueOf(passwordField.getPassword()));
                    userNameField.setText("");
                    passwordField.setText("");
                    latch.countDown();
                }
            });
            return loginButton;
        }

        public Login getLogin() {
            latch = new CountDownLatch(1);

            frame.setVisible(true);
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            frame.setVisible(false);
            return login[0];
        }
    }

    private class MainWindow {
        private final JList<String> chatList;
        private final JList<ChatBox> chatBoxList;
        private final JTextArea messageField;
        private final JButton sendButton;
        private final JPanel messagePane;
        private final JScrollPane chatScrollPane;
        private final JScrollPane inputScrollPane;
        private final JScrollPane chatBoxListScrollPane;
        private final DefaultListModel<String> chatModel;
        private ChatBox chatBox = new ChatBox();
        private final JPanel panel;

        public MainWindow() {
            frame.setSize(600, 500);
            // Create the chat area (used to display messages)
            chatModel = new DefaultListModel<>();
            chatList = new JList<>(chatModel);
            ChatBox chatBox = new ChatBox();

            chatList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
//        chatArea.setEditable(false);  // Messages should not be editable

            // Create the scroll pane for the chat area
            chatScrollPane = new JScrollPane(chatList);
            chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            // Create the message input field

            messageField = new JTextArea();
            messageField.setLineWrap(true);
            messageField.setWrapStyleWord(true);
            inputScrollPane = new JScrollPane(messageField);
            inputScrollPane.setPreferredSize(new Dimension(500, 60));
            messagePane = new JPanel();
            messagePane.add(inputScrollPane);

            // Create the send button
            sendButton = new JButton("Send");
            messagePane.add(sendButton);
            frame.setResizable(false);

            // Create the List to display and select chatboxes.
            chatBoxList = new JList<>(treeListModel);
            chatBoxList.setPreferredSize(new Dimension(200, chatBoxList.getPreferredSize().height));
            chatBoxListScrollPane = new JScrollPane(chatBoxList);

            // Action listener for the send button
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });

            // Create the layout and add components
            panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(chatScrollPane, BorderLayout.CENTER);
            panel.add(new JPanel(), BorderLayout.SOUTH);
            panel.add(messagePane, BorderLayout.SOUTH);
            panel.add(chatBoxListScrollPane, BorderLayout.WEST);

        }

        public void selectChatBox(ChatBox chatBox) {
            this.chatBox = chatBox;
            addAllMessages(this.chatBox);
        }

        public void sendMessage() {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                client.queueMessage(new SendMessage(new Message(client.getUserData()
                                                                      .getUserID(), message), chatBox.getChatBoxID()));
                messageField.setText("");// Clear the input field
            }
        }

    }

    // New inner class for the connection window
    private static class ConnectionWindow {
        private final JFrame frame;
        private final JTextField serverIPField;
        private final JTextField serverPortField;
        private CountDownLatch latch;
        private final ConnectionInfo[] connectionInfo;

        public ConnectionWindow() {
            frame = new JFrame("Connect to Server");
            connectionInfo = new ConnectionInfo[1];
            JPanel serverIPPanel = new JPanel();
            serverIPPanel.setLayout(new FlowLayout());
            serverIPPanel.add(new JLabel("Server IP:"));
            serverIPField = new JTextField();
            serverIPField.setPreferredSize(new Dimension(150, serverIPField.getPreferredSize().height));

            // Set default IP address (localhost)
            serverIPField.setText(getLocalIPAddress());

            serverIPPanel.add(serverIPField);

            JPanel serverPortPanel = new JPanel();
            serverPortPanel.setLayout(new FlowLayout());
            serverPortPanel.add(new JLabel("Server Port:"));
            serverPortField = new JTextField();
            serverPortField.setPreferredSize(new Dimension(150, serverPortField.getPreferredSize().height));

            // Set default port number
            serverPortField.setText("1234");

            serverPortPanel.add(serverPortField);
            JButton connectButton = getConnectButton();

            JPanel connectPanel = new JPanel();
            connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
            connectPanel.add(serverIPPanel);
            connectPanel.add(serverPortPanel);
            connectPanel.add(connectButton);

            frame.getContentPane().add(connectPanel);
            frame.setAlwaysOnTop(true);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setResizable(false);
        }

        private String getLocalIPAddress() {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "127.0.0.1"; // Fallback to localhost
            }
        }

		private JButton getConnectButton() {
            JButton connectButton = new JButton("Connect");
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String serverIP = serverIPField.getText();
                    String serverPort = serverPortField.getText();
                    int port;
                    try {
                        port = Integer.parseInt(serverPort);
                        connectionInfo[0] = new ConnectionInfo(serverIP, port);
                        serverIPField.setText("");
                        serverPortField.setText("");
                        latch.countDown();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid port number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            return connectButton;
        }

        public ConnectionInfo getConnectionInfo() {
            latch = new CountDownLatch(1);

            frame.setVisible(true);
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            frame.setVisible(false);
            return connectionInfo[0];
        }
    }

    private class TreeListModel<E> extends AbstractListModel<E> {
        private final TreeSet<E> treeSet;

        public TreeListModel(Comparator<E> comparator) {
            treeSet = new TreeSet<>(comparator);
        }

        public void add(E item) {
            treeSet.add(item);
            int index = treeSet.stream().toList().indexOf(item);
            fireIntervalAdded(this, index, index);
        }
        public boolean isEmpty() {
            return treeSet.isEmpty();
        }

        public boolean contains(E item) {
            return treeSet.contains(item);
        }

        public void addAll(Collection<? extends E> items) {
            treeSet.addAll(items);
            fireIntervalAdded(this, 0, -1);
        }

        public void remove(E item) {
            int index = treeSet.stream().toList().indexOf(item);
            treeSet.remove(item);
            super.fireIntervalRemoved(this, index, index);
        }

        public void clear() {
            treeSet.clear();
            fireIntervalRemoved(this, 0, -1);
        }


        @Override
        public int getSize() {
            return treeSet.size();
        }

        @Override
        public E getElementAt(int index) {
            return treeSet.stream().toList().get(index);
        }
    }
    
    public class CreateChatBoxDialog extends JDialog implements ActionListener
    {
    	private static CreateChatBoxDialog dialog; 
    	
    	//Precondition: pane must be the content pane of the JDialog
    	//Postcondition: add all contents to content pane in proper layout
    	public static void addToContentPane(Container pane)
    	{
    		
    		//BoxLayout elements
    		
    		JPanel comboPanel = setupBoxLayout();
    		
    		
    		//BorderLayout elements
    		
    		setupLayout(pane, comboPanel);
    		
    		
    		
    	}
    	
    	//Precondition: None
    	//Post: sets up the box layout portion of the content pane
    	private static JPanel setupBoxLayout()
    	{
    		JPanel comboPanel = new JPanel();
    		comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS)); // set layout
    		
    		JTextField chBoxTxt = new JTextField();
    		chBoxTxt.setAlignmentX(Component.CENTER_ALIGNMENT);
    		
    		JLabel chBoxName = new JLabel("Name Inserted Here");
    		
    		
    		JButton createButton = new JButton ("Create ChatBox");
    		createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    		
    		
    		
    		comboPanel.add(chBoxTxt);
    		comboPanel.add(chBoxName);
    		comboPanel.add(createButton);
    		
    		return comboPanel;
    	}
    	
    	//Precondition: pane must be the content pane of the JDialog
    	//Postcondition: finished up on BorderLayout and brings it all together
    	private static void setupLayout(Container pane, JPanel comboPanel)
    	{
    		JLabel prompt = new JLabel("Users are listed on the left, Participants on the right\n"
    				+ "Below that we have name of new ChatBox on the left and create ChatBox button to the right"); 
    		
    		JList users = new JList();
    		users.setPreferredSize( new Dimension (200,100));
    		JList participants = new JList();
    		participants.setPreferredSize( new Dimension (200,100));
    		
    		pane.add(prompt, BorderLayout.NORTH); // add prompt
    		pane.add(users, BorderLayout.CENTER); // add user list
    		pane.add(participants, BorderLayout.EAST); //add participants list
    		
    		pane.add(comboPanel, BorderLayout.SOUTH); // add text and button
    		
    	}
    	
    	
    	
    	public void actionPerformed(ActionEvent e) {
    		
    		/*
    		 * How to detect source of event
    		 * 
    		 * if (evt.getSource().equals(textField))
    			{
    				System.out.println("text field used");
    			}
    		
    		*/
            if ("Create ChatBox".equals(e.getActionCommand())) {
            	
            	//create chatbox request stuff
            	
            	
            	
            	//Close dialog
            	
            	dialog.setVisible(false);
            	
            }
            
        }
    	
    	
    }
}
