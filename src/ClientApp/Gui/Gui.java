package ClientApp.Gui;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import ClientApp.Client.Client;
import Common.Messages.AskChatBox;
import Common.Messages.Login;
import Common.Messages.SendMessage;
import Common.ChatBox.ChatBox;
import Common.Message.Message;
import Common.User.User;
import java.util.List;

public class Gui {
    private final Client client;
    private final JFrame frame;
    private final LoginWindow loginWindow;
    private final MainWindow mainWindow;
    private final ConnectionWindow connectionWindow; // Added connection window
    private final TreeListModel<ChatBox> treeListModel;
    
  
    public Gui(Client client) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.client = client;
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);// Center the window
        treeListModel = new TreeListModel<>(Comparator.comparing(ChatBox::lastUpdated)
                                                      .thenComparing(ChatBox::getChatBoxID));

        loginWindow = new LoginWindow();
        mainWindow = new MainWindow();
        connectionWindow = new ConnectionWindow();// Initialize connection window
    }

    // Method to get connection info from the user
    public ConnectionInfo getConnectionInfo() {
        return connectionWindow.getConnectionInfo();
    }

    public void showMain() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().add(mainWindow.panel);

            if (!treeListModel.isEmpty()) {
                mainWindow.chatBox = treeListModel.getElementAt(0);
            }
            frame.setLocationRelativeTo(null);
            frame.setSize(new Dimension(1000, 600));
            show();
        });

    }

    public void addChatBox(ChatBox chatBox) {
        if(mainWindow.chatBox==null) {mainWindow.chatBox = chatBox;}
        SwingUtilities.invokeLater(() -> treeListModel.add(chatBox));
    }
    public void addAllChatBoxes(Collection<? extends ChatBox> chatBoxes) {
        SwingUtilities.invokeLater(() -> {
            treeListModel.addAll(chatBoxes);
            mainWindow.chatBox = treeListModel.getElementAt(0);
        });
    }

    public boolean hasChatBoxes(){
        return !treeListModel.isEmpty();
    }

    public boolean containsChatBox(ChatBox chatBox) {
        return treeListModel.contains(chatBox);
    }
    public void clearMessages(){
        SwingUtilities.invokeLater(mainWindow.chatModel::clear);
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
        SwingUtilities.invokeLater(() -> {
            mainWindow.chatModel.addElement("<html><b>" + username + ": </b>" +
                                            message.toString().replace("\n", "<br><plaintext>     </plaintext>") +
                                            "<br></html>");
        });
    }

    public ChatBox getChatBox() {
        return mainWindow.chatBox;
    }

    public ChatBox getChatBox(int chatBoxID) {
        return treeListModel
                .treeSet
                .stream()
                .filter(chatBox -> chatBox.getChatBoxID()==chatBoxID)
                .findFirst()
                .orElse(null);

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

    public void addAllMessages(ChatBox chatBox) {
       for(var i:chatBox.getMessages()){
           addMessage(i);
       }
    }
  
  



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
            userNameField = new JTextField("bob admin");
            userNameField.setPreferredSize(new Dimension(150, userNameField.getPreferredSize().height));
            usernamePanel.add(userNameField);
            JPanel passwordPanel = new JPanel();
            passwordPanel.setLayout(new FlowLayout());
            passwordPanel.add(new JLabel("Password:"));
            passwordField = new JPasswordField("BobPass");
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
            loginButton.addActionListener(e -> {
                login[0] = new Login(userNameField.getText(), String.valueOf(passwordField.getPassword()));
                userNameField.setText("");
                passwordField.setText("");
                latch.countDown();
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
        private ChatBox lastSelectedChatBox;
        private ChatBox chatBox = null;
        private final JPanel panel;

        public MainWindow() {
            frame.setSize(600, 500);
            // Create the chat area (used to display messages)
            chatModel = new DefaultListModel<>();
            chatList = new JList<>(chatModel);
            chatList.setSelectionModel(new DefaultListSelectionModel());



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
            chatBoxList.setSelectionModel(new DefaultListSelectionModel());
            chatBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);




            // Action listener for the send button
            sendButton.addActionListener(e -> sendMessage());

            // Action listener for the ChatBox list
            chatBoxList.addListSelectionListener(e->{
                ChatBox selectedChatBox = chatBoxList.getSelectedValue();
                if (selectedChatBox != null && !e.getValueIsAdjusting()) {
                System.out.println(chatBoxList.getSelectedValue().getName() + " Selected");
                    selectChatBox(selectedChatBox);
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
            if(this.chatBox != chatBox) {
                this.chatBox = chatBox;
                clearMessages();
                client.queueMessage(new AskChatBox(chatBox.getChatBoxID()));
            }
        }

        public void sendMessage() {
            String message = messageField.getText().strip().trim().replaceAll("(?m)^\\s+$","");
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
            connectButton.addActionListener(e -> {
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


    private static class TreeListModel<E> extends AbstractListModel<E> {
        private final TreeSet<E> treeSet;

        public TreeListModel(Comparator<E> comparator) {
            treeSet = new TreeSet<>(comparator.reversed());
        }

        public void addQuietly(E item){
            treeSet.removeIf(item::equals);
            treeSet.add(item);
        }
        public void add(E item) {
            treeSet.removeIf(item::equals);
            treeSet.add(item);
            int index = treeSet.stream().toList().indexOf(item);
            fireIntervalAdded(this, index, index);
        }

        public boolean isEmpty() {
            return treeSet.isEmpty();
        }

        public boolean contains(E item) {
            return treeSet.stream().toList().contains(item);
        }

        public void addAll(Collection<? extends E> items) {
            if(items == null || items.isEmpty()) {return;}
            treeSet.addAll(items);
            fireIntervalAdded(this, 0, treeSet.size()-1);
        }

        public void remove(E item) {
            int index = treeSet.stream().toList().indexOf(item);
            if(treeSet.removeIf(item::equals)) {
                super.fireIntervalRemoved(this, index, index);
            }
        }

        public void clear() {
            if(treeSet.isEmpty()) {return;}
            int end = treeSet.size()-1;
            treeSet.clear();
            fireIntervalRemoved(this, 0, end);
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
    
    public class CreateChatBoxDialog extends JDialog
    {
    	
    	/*
    	 * TO DO:
    	 * 
    	 * 
    	 */
    	
 
    	
    	private static JPanel comboPanel;
    	private static Container pane; //content pane of dialog
    	private static JTextField chBoxTxt;
    	private static JLabel chBoxName;
    	private static JButton createButton;
    	private static JButton addParticipant;
    	private static JButton removeParticipant;
    	private static JList<User> users;
    	private static JList<User> participants;
    	private static JLabel prompt;
    	private static DefaultListModel<User> userModel;
    	private static DefaultListModel<User> participantModel;
    	private static int [] userListIndex;
    	private static int [] participantListIndex;
    	private static String chatboxName;
    	
    	
    	private static CreateChatBoxDialog dialog;
    	
    	public CreateChatBoxDialog( JFrame inFrame )
    	{
    		super (inFrame, "createChatBox", true);
    		
    		dialog =  this;
    		
    		
    		//Call Initialization
    		
    		if (  Setup()  )
    		{
    			
    		}
    		else
    		{
    			
    		}
    		
    		//ready to display
    		
    		this.pack();
    		this.setLocationRelativeTo(null);
    		this.setVisible(true);
    		
    	}
    	
    	public static boolean Setup()
    	{
    		
    		return true;
    		
    	}
    	
    	
    	//Precondition: pane must be the content pane of the JDialog
    	//Postcondition: add all contents to content pane in proper layout
    	public static void setUpContentPane()
    	{
    		
    		//BoxLayout elements
    		
    		comboPanel = setupBoxLayout();
    		
    		
    		//BorderLayout elements
    		
    		setupLayout();
    		
    		
    		
    	}
    	
    	//Precondition: None
    	//Post: sets up the box layout portion of the content pane
    	private static JPanel setupBoxLayout()
    	{
    		comboPanel = new JPanel();
    		comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS)); // set layout
    		
    		chBoxTxt = new JTextField(20);
    		chBoxTxt.setAlignmentX(Component.CENTER_ALIGNMENT);
    		chBoxTxt.addActionListener( new TxtBoxListener()  );
    		
    		chBoxName = new JLabel("Name Inserted Here");
    		
    		
    		createButton = new JButton ("Create ChatBox");
    		createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    		createButton.addActionListener(  new CreateButtonListener()  );
    		
    		addParticipant = new JButton("Add Participant From List");
    		addParticipant.setAlignmentX(Component.CENTER_ALIGNMENT);
    		addParticipant.addActionListener( new AddButtonListener()  );
    		
    		removeParticipant = new JButton("Remove Participant From List");
    		removeParticipant.setAlignmentX(Component.CENTER_ALIGNMENT);
    		removeParticipant.addActionListener( new RemoveButtonListener() );
    		
    		comboPanel.add(chBoxTxt);
    		comboPanel.add(chBoxName);
    		comboPanel.add(createButton);
    		comboPanel.add(addParticipant);
    		comboPanel.add(removeParticipant);
    		
    		return comboPanel;
    	}
    	
    	//Precondition: pane must be the content pane of the JDialog
    	//Postcondition: finished up on BorderLayout and brings it all together
    	private static void setupLayout()
    	{
    		prompt = new JLabel("Users are listed on the left, Participants on the right\n"
    				+ "Below that we have name of new ChatBox on the left and create ChatBox button to the right"); 
    		
    		
    		if ( ! setUpUserList() )
    		{
    			//error need to stop trying to open
    		}
    		else
    		{
    			setUpParticipantList();
        		
        		pane.add(prompt, BorderLayout.NORTH); // add prompt
        		pane.add(users, BorderLayout.CENTER); // add user list
        		pane.add(participants, BorderLayout.EAST); //add participants list
        		
        		pane.add(comboPanel, BorderLayout.SOUTH); // add text and button
    		}
    		
    		
    	}
    	
    	
    	
    	
    	
    	
		
		public static boolean setUpUserList() //Needs a way to grab Users
    	{
    		userModel = new DefaultListModel<User>();
    		
    		//try to add to userModel, if we cannot or its empty then we must stop trying to open the dialog
    		
    		
    		ArrayList <User> userList = new ArrayList<User>(); //grab userlist from server *** MUST DO ****
    		
    		if ( userList == null || userList.isEmpty() )
    		{
    			return false;
    		}
    		else
    		{
    			for (int i = 0; i < userList.size(); i++)
        		{
        			userModel.addElement(userList.get(i)); //add the user list to the GUI container
        		}
    		}
    		
    		
    		
    		users = new JList<User>(userModel);
    		
    		users.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    		users.setSelectedIndex(0);
    		users.addListSelectionListener(new UserListListener());
    		users.setVisibleRowCount(10);
    		
    		//Initialize list
    		
    		return true;
    		
    	}
    	
    	public static void setUpParticipantList()
    	{
    		participantModel = new DefaultListModel<User>();
    		participants = new JList<User>(participantModel);
    		participants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    		participants.setSelectedIndex(0);
    		participants.addListSelectionListener(new ParticipantListListener());
    		
    		
    	}
    	
    	//Handles when user is clicking on the list of users
    	public static class UserListListener implements ListSelectionListener
    	{
    		
			
			public void valueChanged(ListSelectionEvent e) {
				
				if (e.getValueIsAdjusting() == false)
				{
					//get indexes
					
					
					userListIndex = users.getSelectedIndices(); //array is empty if nothing is selected
					
					if (userListIndex.length == 0)
					{
						addParticipant.setEnabled(false); //nothing selected, disable button
					}
					else
					{
						addParticipant.setEnabled(true); // item(s) selected enable array
					}
					
				}
			}
    		
    	}
    	
    	//handles when user is clicking on the list of participants
    	public static class ParticipantListListener implements ListSelectionListener
    	{
    		
			
			public void valueChanged(ListSelectionEvent e) {
				
				if (e.getValueIsAdjusting() == false)
				{
					participantListIndex = participants.getSelectedIndices();
					
					if (participantListIndex.length == 0) //nothing selected, disable button
					{
						removeParticipant.setEnabled(false);
					}
					else
					{
						removeParticipant.setEnabled(true);
					}
				}
			}
    		
    	}
    	
    	//Handles when user is clicking on the AddParticipants button
    	public static class AddButtonListener implements ActionListener
    	{

		
			public void actionPerformed(ActionEvent e) { //user model transfer
				
				//userListIndex - index containing users
				//userModel - container storing the Users itself
				//participantModel - container storing the Participants itself
				//users - JList
				
				ArrayList<User> temporary = new ArrayList<User> ();
				
				//grab the users from the UI list of users
				
				
				for (int i = 0; i < userListIndex.length; i++)
				{
					
					temporary.add(userModel.get(  userListIndex[i] ));
					
				}
				
				//add the list to the participants UI list
				
				for (int j = 0; j < temporary.size(); j++)
				{
					
					//check if it exists already
					
					if ( ! participantModel.contains( temporary.get(j) ) ) //duplicate check
					{
						participantModel.addElement( temporary.get(j) );
					}
					
					
					
				}
				
				
				
				
			}
			
    		
    	}
    	
    	//Handles when user is clicking on the RemoveParticipants button
    	public static class RemoveButtonListener implements ActionListener
    	{
    		//participantListIndex - index containing participants
		
			public void actionPerformed(ActionEvent e) {
				
				// grab list of participants to be removed
				// participantModel - container storing the Participants itself
				
				
				
				for (int i = 0; i < participantListIndex.length; i++)
				{
					participantModel.remove( participantListIndex[i] ); //remove participants
				}
				
				
		
			}
    		
    	}
    	
    	//ChatBox Name Listener
    	//Upon hitting "enter" when typing in the textfield, the label will update with chatbox name
    	public static class TxtBoxListener implements ActionListener
    	{
    		
    		/*
    			private static JTextField chBoxTxt;
    			private static JLabel chBoxName;
    			private static String chatboxName;
    		 */
    		
			public void actionPerformed(ActionEvent e) {
				
				chatboxName = chBoxTxt.getText(); //set chatbox name
				chBoxName.setText(chatboxName); //set the label
			}
    		
    	}
    	
    	
    	//Create ChatBox button
    	public static class CreateButtonListener implements ActionListener
    	{
    		/*
        	 * private static DefaultListModel<User> participantModel;
        	 * private static String chatboxName;
        	*/
			
			public void actionPerformed(ActionEvent e) {
				
				//grab the info for participants list and chatbox name
				
				ArrayList<User> temp = new ArrayList<User>();
				
				for (int i = 0; i < participantModel.getSize(); i++)
				{
					temp.add( participantModel.get(i) );
				}
				
				//Make request to send chatbox
				
				
				
				//.queueMessage(new CreateChat(participants, name))
				
				
				//close the dialog
				
				dialog.setVisible(false);
				
			}
    		
    	}
    	
    	
    	
    	
    }
    
    
}
