package ClientApp.Gui;

import javax.swing.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ClientApp.Client.Client;
import Common.Admin.Admin;
import Common.ChatBox.ChatBox;
import Common.Message.Message;
import Common.Messages.*;
import Common.User.User;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Gui {
    private final Client client;
    private final JFrame frame;
    private final LoginWindow loginWindow;
    private final MainWindow mainWindow;
    private final ConnectionWindow connectionWindow;
    private final TreeListModel<ChatBox> treeListModel;
    private final CreateChatBoxDialog chatBoxDialog;
    private final AdminOptionsWindow adminOptionsWindow;
    private final DefaultListModel<User> userModel;

    // Define color scheme
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private static final Color PANEL_COLOR = new Color(230, 230, 250); // Lavender
    private static final Color MENU_COLOR = new Color(220, 220, 220); // Light gray
    private static final Color BUTTON_COLOR = new Color(200, 200, 200); // Gray
    private static final Color CHAT_AREA_COLOR = new Color(255, 255, 255); // White
    private static final Color MESSAGE_INPUT_COLOR = new Color(255, 255, 255); // White

    public Gui(Client client) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Optionally set default background color
            // UIManager.put("Panel.background", BACKGROUND_COLOR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.client = client;
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the window
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        treeListModel = new TreeListModel<>(
                Comparator.comparing(ChatBox::lastUpdated).thenComparing(ChatBox::getChatBoxID));
        userModel = new DefaultListModel<>();

        loginWindow = new LoginWindow();
        mainWindow = new MainWindow();
        connectionWindow = new ConnectionWindow();

        chatBoxDialog = new CreateChatBoxDialog(frame);
        adminOptionsWindow = new AdminOptionsWindow(frame);
    }

    public void updateChatBox(ChatBox chatBox) {
        SwingUtilities.invokeLater(() -> {
            treeListModel.remove(chatBox);
            if (!chatBox.isHidden()) {
                treeListModel.add(chatBox);
            }

            if (mainWindow.chatBox != null && mainWindow.chatBox.getChatBoxID() == chatBox.getChatBoxID()) {
                if (chatBox.isHidden()) {
                    // If the current chatbox is hidden, clear messages and set chatbox to null
                    clearMessages();
                    mainWindow.chatBox = null;
                    mainWindow.chatLabel.setText("ChatBox Hidden");
                } else {
                    setChatBox(chatBox);
                }
            }
        });
    }

    public void setChatBox(ChatBox chatBox) {
        mainWindow.chatBox = chatBox;
        mainWindow.chatLabel.setText(chatBox.getName() + "  (ID: " + chatBox.getChatBoxID() + ")");
    }

    public void displayChatLog(String chatLog) {
        if (adminOptionsWindow != null && adminOptionsWindow.getChatLogDialog() != null) {
            adminOptionsWindow.getChatLogDialog().displayChatLog(chatLog);
        }
    }

    public void updateChatBoxList(List<ChatBox> chatBoxes) {
        SwingUtilities.invokeLater(() -> {
            if (adminOptionsWindow != null) {
                adminOptionsWindow.showChatBoxListDialog(chatBoxes);
            }
        });
    }

    // Method to get connection info from the user
    public ConnectionInfo getConnectionInfo() {
        return connectionWindow.getConnectionInfo();
    }

    public void showMain() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().add(mainWindow.panel);
            frame.setTitle(frame.getTitle() + " Logged in as: " + client.getUserData().getUsername());

            if (!treeListModel.isEmpty()) {
                setChatBox(treeListModel.getElementAt(0));
            }
            frame.setLocationRelativeTo(null);
            frame.setSize(new Dimension(1000, 600));
            show();
        });
    }

    public void showCreateChat() {
        client.queueMessage(new AskUserList());
        SwingUtilities.invokeLater(() -> {
            chatBoxDialog.setVisible(true);
        });
    }

    public void updateUserList(Collection<User> users) {
        userModel.clear();
        userModel.addAll(users);
    }

    public void addChatBox(ChatBox chatBox) {
        if (!chatBox.isHidden()) {
            if (mainWindow.chatBox == null) {
                setChatBox(chatBox);
            }
            SwingUtilities.invokeLater(() -> treeListModel.add(chatBox));
        }
    }

    public void addAllChatBoxes(Collection<? extends ChatBox> chatBoxes) {
        SwingUtilities.invokeLater(() -> {
            treeListModel.clear();
            for (ChatBox chatBox : chatBoxes) {
                if (!chatBox.isHidden()) {
                    treeListModel.add(chatBox);
                }
            }
            if (!treeListModel.isEmpty()) {
                setChatBox(treeListModel.getElementAt(0));
            }
        });
    }

    public boolean hasChatBoxes() {
        return !treeListModel.isEmpty();
    }

    public boolean containsChatBox(ChatBox chatBox) {
        return treeListModel.contains(chatBox);
    }

    public void clearMessages() {
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

    public void addMessage(Message message) {
        if (mainWindow.chatBox == null) {
            return;
        }
        User user = idToUser(message.getSenderID(), mainWindow.chatBox);
        String resolvedUsername;
        if (user == null) {
            resolvedUsername = String.valueOf(message.getSenderID());
        } else {
            resolvedUsername = user.getUsername();
            if (user.isBanned()) {
                resolvedUsername += " (banned)";
            }
        }
        final String displayUsername = resolvedUsername;
        SwingUtilities.invokeLater(() -> {
            mainWindow.chatModel
                    .addElement("<html><b> &thinsp " + displayUsername
                            + "</b><font size=\"3\" color=\"gray\">&thinsp "
                            + timeFormat(message.getTimestamp())
                            + "</font>"
                            + "<p style=\"width: 500px; margin-left:10px;\">"
                            + message.toString().replace("\n", "<br>")
                            + "</p><br></html>"
                    );
        });
    }

    public String timeFormat(LocalDateTime time) {
        Period dateAgo = Period.between(time.toLocalDate(), LocalDateTime.now().toLocalDate());

        long count;
        String unit;
        if (dateAgo.getYears() > 0) {
            count = dateAgo.getYears();
            unit = ChronoUnit.YEARS.toString();
        } else if (dateAgo.getMonths() > 0) {
            count = dateAgo.getMonths();
            unit = ChronoUnit.MONTHS.toString();
        } else if (dateAgo.getDays() > 0) {
            count = dateAgo.getDays();
            unit = ChronoUnit.DAYS.toString();
        } else {
            Duration timeAgo = Duration.between(time, LocalDateTime.now());
            if (timeAgo.toHours() > 0) {
                count = timeAgo.toHours();
                unit = ChronoUnit.HOURS.toString();
            } else if (timeAgo.toMinutes() > 0) {
                count = timeAgo.toMinutes();
                unit = ChronoUnit.MINUTES.toString();
            } else if (timeAgo.toSeconds() > 0) {
                return "moments ago";
            } else return "now";
        }

        if (count == 1) {
            unit = unit.replaceAll("s", "");
        }
        unit = unit.toLowerCase();

        return count + " " + unit + " ago";

    }

    public ChatBox getChatBox() {
        return mainWindow.chatBox;
    }

    public ChatBox getChatBox(int chatBoxID) {
        return treeListModel.treeSet.stream().filter(chatBox -> chatBox.getChatBoxID() == chatBoxID).findFirst()
                .orElse(null);
    }

    public User idToUser(int userId, ChatBox chatBox) {
        for (var i : chatBox.getParticipantsList()) {
            if (i.getUserID() == userId) {
                return i;
            }
        }
        return null;
    }

    public void show() {
        frame.setVisible(true);
    }

    public void addAllMessages(ChatBox chatBox) {
        for (var i : chatBox.getMessages()) {
            addMessage(i);
        }
    }

    // Show Admin Options window
    public void showAdminOptions() {
        client.queueMessage(new AskUserList());
        SwingUtilities.invokeLater(() -> {
            adminOptionsWindow.setVisible(true);
        });
    }

    private static class LoginWindow {
        private final JFrame frame;
        private final JTextField userNameField;
        private final JPasswordField passwordField;
        private CountDownLatch latch;
        private final Login[] login;

        public LoginWindow() {
            frame = new JFrame("Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setSize(300, 200);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(BACKGROUND_COLOR);

            login = new Login[1];
            JPanel usernamePanel = new JPanel(new FlowLayout());
            usernamePanel.setBackground(BACKGROUND_COLOR);
            usernamePanel.add(new JLabel("Username:"));
            userNameField = new JTextField("Bob Admin");
            userNameField.setPreferredSize(new Dimension(150, userNameField.getPreferredSize().height));
            usernamePanel.add(userNameField);

            JPanel passwordPanel = new JPanel(new FlowLayout());
            passwordPanel.setBackground(BACKGROUND_COLOR);
            passwordPanel.add(new JLabel("Password:"));
            passwordField = new JPasswordField("BobPass");
            passwordField.setPreferredSize(new Dimension(150, passwordField.getPreferredSize().height));
            passwordPanel.add(passwordField);

            JButton loginButton = getLoginButton();

            JPanel loginPanel = new JPanel();
            loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
            loginPanel.setBackground(BACKGROUND_COLOR);
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
            loginButton.setBackground(BUTTON_COLOR);
            loginButton.setFont(new Font("Arial", Font.BOLD, 14));
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
        private ChatBox chatBox = null;
        private final JPanel panel;
        private final JMenuBar menuBar;
        private final JMenu menu;
        private final List<JMenuItem> menuItems;
        private final JPanel rightPanel;
        private final JLabel chatLabel;

        public MainWindow() {
            frame.setSize(600, 500);
            // Create the chat area (used to display messages)
            {
                rightPanel = new JPanel();
                rightPanel.setBackground(PANEL_COLOR);

                chatModel = new DefaultListModel<>();
                chatList = new JList<>(chatModel);
                chatList.setSelectionModel(new DefaultListSelectionModel());
                chatList.setFont(new Font("Arial", Font.PLAIN, 14));
                chatList.setFixedCellWidth(400);
                chatList.setBackground(CHAT_AREA_COLOR);

                // Create the scroll pane for the chat area
                chatScrollPane = new JScrollPane(chatList);
                chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                chatScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                chatScrollPane.getViewport().setBackground(CHAT_AREA_COLOR);
                chatScrollPane.setBorder(BorderFactory.createEmptyBorder());

                chatLabel = new JLabel();
                chatLabel.setFont(new Font("Arial", Font.BOLD, 16));
                chatLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                chatLabel.setBackground(PANEL_COLOR);
                chatLabel.setOpaque(true);
            }

            // Create the message input field
            {
                messageField = new JTextArea();
                messageField.setLineWrap(true);
                messageField.setWrapStyleWord(true);
                messageField.setBackground(MESSAGE_INPUT_COLOR);
                messageField.setFont(new Font("Arial", Font.PLAIN, 14));

                inputScrollPane = new JScrollPane(messageField);
                inputScrollPane.setPreferredSize(new Dimension(500, 60));
                inputScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                messagePane = new JPanel(new BorderLayout());
                messagePane.add(inputScrollPane, BorderLayout.CENTER);
                messagePane.setBackground(BACKGROUND_COLOR);
            }

            // Create the send button
            {
                sendButton = new JButton("Send");
                sendButton.setBackground(BUTTON_COLOR);
                sendButton.setFont(new Font("Arial", Font.BOLD, 14));
                messagePane.add(sendButton, BorderLayout.EAST);
                // Action listener for the send button
                sendButton.addActionListener(e -> sendMessage());
            }

            // Create the List to display and select chatboxes.

            chatBoxList = new JList<>(treeListModel);
            chatBoxList.setPreferredSize(new Dimension(200, chatBoxList.getPreferredSize().height));
            chatBoxList.setBackground(PANEL_COLOR);
            chatBoxList.setFont(new Font("Arial", Font.PLAIN, 14));

            chatBoxListScrollPane = new JScrollPane(chatBoxList);
            chatBoxListScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            chatBoxListScrollPane.getViewport().setBackground(PANEL_COLOR);

            chatBoxList.setSelectionModel(new DefaultListSelectionModel());
            chatBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // Action listener for the ChatBox list
            chatBoxList.addListSelectionListener(e -> {
                ChatBox selectedChatBox = chatBoxList.getSelectedValue();
                if (selectedChatBox != null && !e.getValueIsAdjusting()) {
                    selectChatBox(selectedChatBox);
                }
            });

            // Create menu bar
            menuBar = new JMenuBar();
            menuBar.setBackground(MENU_COLOR);
            menuBar.setBorder(BorderFactory.createLineBorder(Color.GRAY));

            menu = new JMenu("MAIN MENU (click here)");
            menu.setFont(new Font("Arial", Font.BOLD, 14));
            menu.setBackground(MENU_COLOR);

            menuItems = new ArrayList<>();

            // Create "Create Chat" menu item
            JMenuItem createChatMenuItem = new JMenuItem("Create Chat");
            createChatMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
            createChatMenuItem.addActionListener(e -> showCreateChat());
            menuItems.add(createChatMenuItem);

            // Create "Admin Options" menu item
            JMenuItem adminOptionsMenuItem = new JMenuItem("Admin Options");
            adminOptionsMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
            adminOptionsMenuItem.addActionListener(e -> {
                if (client.getUserData() instanceof Admin) {
                    showAdminOptions();
                } else {
                    JOptionPane.showMessageDialog(frame, "Access denied. Admin privileges required.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            menuItems.add(adminOptionsMenuItem);

            // Add menu items to the menu
            for (var menuItem : menuItems) {
                menu.add(menuItem);
            }

            // Create "Logout" menu Item
            JMenuItem logoutMenuItem = new JMenuItem("Logout");
            logoutMenuItem.setFont(new Font("Arial", Font.PLAIN, 14));
            logoutMenuItem.addActionListener(e -> client.queueMessage(new Logout()));
            menu.add(logoutMenuItem);

            menuBar.add(menu);

            // Create the layout and add components
            panel = new JPanel(new BorderLayout());
            panel.setBackground(BACKGROUND_COLOR);

            rightPanel.setLayout(new BorderLayout());
            rightPanel.add(chatLabel, BorderLayout.NORTH);
            rightPanel.add(chatScrollPane, BorderLayout.CENTER);

            panel.add(menuBar, BorderLayout.NORTH);
            panel.add(chatBoxListScrollPane, BorderLayout.WEST);
            panel.add(rightPanel, BorderLayout.CENTER);
            panel.add(messagePane, BorderLayout.SOUTH);
        }

        public void selectChatBox(ChatBox chatBox) {
            if (chatBox.isHidden()) {
                JOptionPane.showMessageDialog(frame, "This chatbox is currently hidden.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (this.chatBox != chatBox) {
                setChatBox(chatBox);
                clearMessages();
                client.queueMessage(new AskChatBox(chatBox.getChatBoxID()));
            }

            // Disable message input for system chatbox if user is not an admin
            if (chatBox.getChatBoxID() == 0 && !(client.getUserData() instanceof Admin)) {
                messageField.setEnabled(false);
                sendButton.setEnabled(false);
            } else {
                messageField.setEnabled(true);
                sendButton.setEnabled(true);
            }
        }

        public void sendMessage() {
            if (chatBox == null || chatBox.isHidden()) {
                JOptionPane.showMessageDialog(frame, "Cannot send messages to a hidden chatbox.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Prevent non-admin users from sending messages to the system chatbox
            if (chatBox.getChatBoxID() == 0 && !(client.getUserData() instanceof Admin)) {
                JOptionPane.showMessageDialog(frame, "You do not have permission to send messages in this chatbox.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String message = messageField.getText().strip().trim().replaceAll("(?m)^\\s+$", "");
            if (!message.isEmpty()) {
                client.queueMessage(new SendMessage(new Message(client.getUserData().getUserID(), message),
                        chatBox.getChatBoxID()));
                messageField.setText(""); // Clear the input field
            }
        }
    }

    // Inner class for the connection window
    private static class ConnectionWindow {
        private final JFrame frame;
        private final JTextField serverIPField;
        private final JTextField serverPortField;
        private CountDownLatch latch;
        private final ConnectionInfo[] connectionInfo;

        public ConnectionWindow() {
            frame = new JFrame("Connect to Server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setSize(300, 200);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(BACKGROUND_COLOR);

            connectionInfo = new ConnectionInfo[1];

            JPanel serverIPPanel = new JPanel(new FlowLayout());
            serverIPPanel.setBackground(BACKGROUND_COLOR);
            serverIPPanel.add(new JLabel("Server IP:"));
            serverIPField = new JTextField();
            serverIPField.setPreferredSize(new Dimension(150, serverIPField.getPreferredSize().height));

            // Set default IP address (localhost)
            serverIPField.setText(getLocalIPAddress());

            serverIPPanel.add(serverIPField);

            JPanel serverPortPanel = new JPanel(new FlowLayout());
            serverPortPanel.setBackground(BACKGROUND_COLOR);
            serverPortPanel.add(new JLabel("Server Port:"));
            serverPortField = new JTextField();
            serverPortField.setPreferredSize(new Dimension(150, serverPortField.getPreferredSize().height));

            // Set default port number
            serverPortField.setText("1234");

            serverPortPanel.add(serverPortField);
            JButton connectButton = getConnectButton();

            JPanel connectPanel = new JPanel();
            connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.Y_AXIS));
            connectPanel.setBackground(BACKGROUND_COLOR);
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
            connectButton.setBackground(BUTTON_COLOR);
            connectButton.setFont(new Font("Arial", Font.BOLD, 14));
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

        public void addQuietly(E item) {
            treeSet.removeIf(item::equals);
            treeSet.add(item);
        }

        public void add(E item) {
            treeSet.removeIf(item::equals);
            treeSet.add(item);
            int index = new ArrayList<>(treeSet).indexOf(item);
            fireIntervalAdded(this, index, index);
        }

        public boolean isEmpty() {
            return treeSet.isEmpty();
        }

        public boolean contains(E item) {
            return treeSet.contains(item);
        }

        public void addAll(Collection<? extends E> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            treeSet.clear();
            treeSet.addAll(items);
            fireContentsChanged(this, 0, treeSet.size() - 1);
        }

        public void remove(E item) {
            int index = new ArrayList<>(treeSet).indexOf(item);
            if (treeSet.removeIf(item::equals)) {
                super.fireIntervalRemoved(this, index, index);
            }
        }

        public void clear() {
            if (treeSet.isEmpty()) {
                return;
            }
            int end = treeSet.size() - 1;
            treeSet.clear();
            fireIntervalRemoved(this, 0, end);
        }

        @Override
        public int getSize() {
            return treeSet.size();
        }

        @Override
        public E getElementAt(int index) {
            return new ArrayList<>(treeSet).get(index);
        }
    }

    public class CreateChatBoxDialog extends JDialog {

        private JPanel comboPanel;
        private final Container pane; // content pane of dialog
        private JTextField chBoxTxt;
        private JLabel chBoxName;
        private JButton createButton;
        private JButton addParticipant;
        private JButton removeParticipant;
        private JList<User> users;
        private JList<User> participants;
        private JLabel prompt;
        private DefaultListModel<User> participantModel;
        private int[] userListIndex;
        private int[] participantListIndex;
        private String chatboxName;
        private JScrollPane userScrPane;
        private JScrollPane participantScrPane;

        public CreateChatBoxDialog(JFrame inFrame) {
            super(inFrame, "Create ChatBox", true);

            pane = getContentPane(); // set content pane

            // Call Initialization
            setUpContentPane();

            // Ready to display
            this.pack();
            this.setLocationRelativeTo(null);
        }

        // Precondition: pane must be the content pane of the JDialog
        // Postcondition: add all contents to content pane in proper layout
        public void setUpContentPane() {

            // BoxLayout elements
            comboPanel = setupBoxLayout();

            // BorderLayout elements
            setupLayout();
        }

        // Precondition: None
        // Post: sets up the box layout portion of the content pane
        private JPanel setupBoxLayout() {
            comboPanel = new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS)); // set layout

            chBoxTxt = new JTextField(20);

            chBoxTxt.addActionListener(new TxtBoxListener());

            chBoxName = new JLabel("Name Inserted Here");

            createButton = new JButton("Create ChatBox");

            createButton.addActionListener(new CreateButtonListener());

            addParticipant = new JButton("Add Participant From List");
            addParticipant.addActionListener(new AddButtonListener());

            removeParticipant = new JButton("Remove Participant From List");
            removeParticipant.addActionListener(new RemoveButtonListener());

            comboPanel.add(chBoxTxt);
            comboPanel.add(chBoxName);
            comboPanel.add(createButton);
            comboPanel.add(addParticipant);
            comboPanel.add(removeParticipant);

            comboPanel.setOpaque(true);

            return comboPanel;
        }

        // Precondition: pane must be the content pane of the JDialog
        // Postcondition: finished up on BorderLayout and brings it all together
        private void setupLayout() {
            prompt = new JLabel("Users are listed on the left, Participants on the right\n"
                    + "Below that we have name of new ChatBox on the left and create ChatBox button to the right");

            setUpUserList();

            setUpParticipantList();

            pane.add(prompt, BorderLayout.NORTH); // add prompt
            pane.add(userScrPane, BorderLayout.CENTER); // add user list
            pane.add(participantScrPane, BorderLayout.EAST); // add participants list

            pane.add(comboPanel, BorderLayout.SOUTH); // add text and button
        }

        public void setUpUserList() {
            users = new JList<>(userModel);
            users.setCellRenderer(new UserListCellRenderer()); // Set the cell renderer

            users.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            users.setSelectedIndex(0);
            users.addListSelectionListener(new UserListListener());
            users.setVisibleRowCount(10);

            // Initialize list
            userScrPane = new JScrollPane(users, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            userScrPane.setOpaque(true);
        }

        public void setUpParticipantList() {
            participantModel = new DefaultListModel<>();
            participants = new JList<>(participantModel);
            participants.setCellRenderer(new UserListCellRenderer()); // Set the cell renderer
            participants.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            participants.setSelectedIndex(0);
            participants.addListSelectionListener(new ParticipantListListener());

            participantModel.clear();

            participantScrPane = new JScrollPane(participants, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            participantScrPane.setOpaque(true);
        }

        // Handles when user is clicking on the list of users
        public class UserListListener implements ListSelectionListener {

            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    // get indexes

                    userListIndex = users.getSelectedIndices(); // array is empty if nothing is selected

                    if (userListIndex.length == 0) {
                        addParticipant.setEnabled(false); // nothing selected, disable button
                    } else {
                        addParticipant.setEnabled(true); // item(s) selected enable array
                    }

                }
            }

        }

        // handles when user is clicking on the list of participants
        public class ParticipantListListener implements ListSelectionListener {

            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    participantListIndex = participants.getSelectedIndices();

                    if (participantListIndex.length == 0) // nothing selected, disable button
                    {
                        removeParticipant.setEnabled(false);
                    } else {
                        removeParticipant.setEnabled(true);
                    }
                }
            }

        }

        // Handles when user is clicking on the AddParticipants button
        public class AddButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                ArrayList<User> temporary = new ArrayList<>();

                // Grab the users from the UI list of users
                for (int listIndex : userListIndex) {
                    User selectedUser = userModel.get(listIndex);
                    if (selectedUser.isBanned()) {
                        JOptionPane.showMessageDialog(CreateChatBoxDialog.this,
                                "Cannot add banned user: " + selectedUser.getUsername(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                        continue; // Skip adding this user
                    }
                    temporary.add(selectedUser);
                }

                // Add the list to the participants UI list
                for (User user : temporary) {
                    if (!participantModel.contains(user)) {
                        participantModel.addElement(user);
                    }
                }
            }
        }

        // Handles when user is clicking on the RemoveParticipants button
        public class RemoveButtonListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                // grab list of participants to be removed

                for (int listIndex : participantListIndex) {
                    participantModel.remove(listIndex); // remove participants
                }

            }

        }

        // ChatBox Name Listener
        // Upon hitting "enter" when typing in the textfield, the label will update with chatbox name
        public class TxtBoxListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                chatboxName = chBoxTxt.getText(); // set chatbox name
                chBoxName.setText(chatboxName); // set the label
                chBoxTxt.selectAll(); // highlights the text field

            }

        }

        // Create ChatBox button
        public class CreateButtonListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                // grab the info for participants list and chatbox name

                ArrayList<User> participantList = new ArrayList<>();

                for (int i = 0; i < participantModel.getSize(); i++) {
                    participantList.add(participantModel.get(i));
                }

                // Make request to send chatbox
                client.queueMessage(new CreateChat(participantList, chatboxName));

                // close the dialog
                setVisible(false);
            }
        }
    }

    // AdminOptionsWindow implemented similarly to CreateChatBoxDialog
    public class AdminOptionsWindow extends JDialog {

        private JPanel comboPanel;
        private final Container pane; // content pane of dialog
        private JButton banUserButton;
        private JButton unbanUserButton;
        private JButton viewChatLogButton;
        private JButton createUserButton;
        private JButton hideChatBoxButton; // Add Hide ChatBox button
        private JButton unhideChatBoxButton; // Add Unhide ChatBox button
        private JList<User> users;
        private JLabel prompt;
        private int[] userListIndex;
        private JScrollPane userScrPane;
        private ChatLogDialog chatLogDialog;
        private ChatBoxListDialog chatBoxListDialog;

        public AdminOptionsWindow(JFrame inFrame) {
            super(inFrame, "Admin Options", true);

            pane = getContentPane(); // set content pane

            // Call Initialization
            setUpContentPane();

            // Ready to display
            this.pack();
            this.setLocationRelativeTo(null);
        }

        public void showChatLogDialog(List<ChatBox> chatBoxes) {
            chatLogDialog = new ChatLogDialog(this, chatBoxes, client);
            chatLogDialog.setVisible(true);
        }

        public ChatLogDialog getChatLogDialog() {
            return chatLogDialog;
        }

        public void showChatBoxListDialog(List<ChatBox> chatBoxes) {
            chatBoxListDialog = new ChatBoxListDialog(this, chatBoxes, client);
            chatBoxListDialog.setVisible(true);
        }

        public class ChatLogDialog extends JDialog {
            private final JList<ChatBox> chatBoxList;
            private final DefaultListModel<ChatBox> chatBoxListModel;
            private final JTextArea chatLogArea;
            private final JButton getLogButton;
            private final JButton closeButton;
            private final Client client;

            public ChatLogDialog(AdminOptionsWindow adminOptionsWindow, List<ChatBox> chatBoxes, Client client) {
                super(adminOptionsWindow, "Chat Logs", true);
                this.client = client;

                chatBoxListModel = new DefaultListModel<>();
                chatBoxListModel.addAll(chatBoxes);

                chatBoxList = new JList<>(chatBoxListModel);
                chatBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                chatLogArea = new JTextArea();
                chatLogArea.setEditable(false);

                getLogButton = new JButton("Get Log");
                getLogButton.addActionListener(e -> requestChatLog());

                closeButton = new JButton("Close");
                closeButton.addActionListener(e -> dispose());

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(getLogButton);
                buttonPanel.add(closeButton);

                setLayout(new BorderLayout());
                add(new JScrollPane(chatBoxList), BorderLayout.WEST);
                add(new JScrollPane(chatLogArea), BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);

                setSize(600, 400);
                setLocationRelativeTo(adminOptionsWindow);
            }

            private void requestChatLog() {
                ChatBox selectedChatBox = chatBoxList.getSelectedValue();
                if (selectedChatBox != null) {
                    client.queueMessage(new AskChatLog(selectedChatBox.getChatBoxID()));
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a chatbox.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            public void displayChatLog(String chatLog) {
                SwingUtilities.invokeLater(() -> chatLogArea.setText(chatLog));
            }
        }

        // **ChatBoxListDialog** class for Hide/Unhide ChatBox
        public class ChatBoxListDialog extends JDialog {
            private final JList<ChatBox> chatBoxList;
            private final DefaultListModel<ChatBox> chatBoxListModel;
            private final JButton hideButton;
            private final JButton unhideButton;
            private final JButton closeButton;
            private final Client client;

            public ChatBoxListDialog(AdminOptionsWindow adminOptionsWindow, List<ChatBox> chatBoxes, Client client) {
                super(adminOptionsWindow, "Manage ChatBoxes", true);
                this.client = client;

                chatBoxListModel = new DefaultListModel<>();
                chatBoxListModel.addAll(chatBoxes);

                chatBoxList = new JList<>(chatBoxListModel);
                chatBoxList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                hideButton = new JButton("Hide ChatBox");
                hideButton.addActionListener(e -> hideChatBox());

                unhideButton = new JButton("Unhide ChatBox");
                unhideButton.addActionListener(e -> unhideChatBox());

                closeButton = new JButton("Close");
                closeButton.addActionListener(e -> dispose());

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(hideButton);
                buttonPanel.add(unhideButton);
                buttonPanel.add(closeButton);

                setLayout(new BorderLayout());
                add(new JScrollPane(chatBoxList), BorderLayout.CENTER);
                add(buttonPanel, BorderLayout.SOUTH);

                setSize(400, 300);
                setLocationRelativeTo(adminOptionsWindow);
            }

            private void hideChatBox() {
                ChatBox selectedChatBox = chatBoxList.getSelectedValue();
                if (selectedChatBox != null) {
                    client.queueMessage(new HideChatBox(selectedChatBox.getChatBoxID()));
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a chatbox to hide.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            private void unhideChatBox() {
                ChatBox selectedChatBox = chatBoxList.getSelectedValue();
                if (selectedChatBox != null) {
                    client.queueMessage(new UnhideChatBox(selectedChatBox.getChatBoxID()));
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a chatbox to unhide.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // Precondition: pane must be the content pane of the JDialog
        // Postcondition: add all contents to content pane in proper layout
        public void setUpContentPane() {

            // BoxLayout elements
            comboPanel = setupBoxLayout();

            // BorderLayout elements
            setupLayout();
        }

        // Precondition: None
        // Post: sets up the box layout portion of the content pane
        private JPanel setupBoxLayout() {
            comboPanel = new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS)); // set layout

            banUserButton = new JButton("Ban User");
            banUserButton.addActionListener(new BanUserButtonListener());

            unbanUserButton = new JButton("Unban User");
            unbanUserButton.addActionListener(new UnbanUserButtonListener());

            viewChatLogButton = new JButton("View Chat Logs");
            viewChatLogButton.addActionListener(new ViewChatLogButtonListener());

            hideChatBoxButton = new JButton("Hide ChatBox");
            hideChatBoxButton.addActionListener(new HideChatBoxButtonListener());

            unhideChatBoxButton = new JButton("Unhide ChatBox");
            unhideChatBoxButton.addActionListener(new UnhideChatBoxButtonListener());

            createUserButton = new JButton("Create User"); // Initialize Create User button
            createUserButton.addActionListener(new CreateUserButtonListener());

            comboPanel.add(banUserButton);
            comboPanel.add(unbanUserButton);
            comboPanel.add(viewChatLogButton);
            comboPanel.add(hideChatBoxButton);
            comboPanel.add(unhideChatBoxButton);
            comboPanel.add(createUserButton); // Add Create User button to the panel

            comboPanel.setOpaque(true);

            return comboPanel;
        }

        // Precondition: pane must be the content pane of the JDialog
        // Postcondition: finished up on BorderLayout and brings it all together
        private void setupLayout() {
            prompt = new JLabel("Select a user from the list and choose an action");

            setUpUserList();

            pane.add(prompt, BorderLayout.NORTH); // add prompt
            pane.add(userScrPane, BorderLayout.CENTER); // add user list

            pane.add(comboPanel, BorderLayout.SOUTH); // add buttons
        }

        public void setUpUserList() {
            users = new JList<>(userModel);
            users.setCellRenderer(new UserListCellRenderer()); // Set the cell renderer

            users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            users.setSelectedIndex(0);
            users.addListSelectionListener(new UserListListener());
            users.setVisibleRowCount(10);

            // Initialize list
            userScrPane = new JScrollPane(users, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            userScrPane.setOpaque(true);
        }

        // Handles when user is clicking on the list of users
        public class UserListListener implements ListSelectionListener {

            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    // get indexes

                    userListIndex = users.getSelectedIndices(); // array is empty if nothing is selected

                    boolean hasSelection = userListIndex.length > 0;
                    banUserButton.setEnabled(hasSelection);
                    unbanUserButton.setEnabled(hasSelection);
                    viewChatLogButton.setEnabled(true); // Always enabled
                    hideChatBoxButton.setEnabled(true); // Always enabled
                    unhideChatBoxButton.setEnabled(true); // Always enabled
                }
            }

        }

        // Handles when user clicks on Ban User button
        public class BanUserButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                User selectedUser = users.getSelectedValue();
                if (selectedUser != null) {
                    if (selectedUser.equals(client.getUserData())) {
                        JOptionPane.showMessageDialog(frame, "You can't ban yourself.");
                        return;
                    }
                    client.queueMessage(new BanUser(selectedUser.getUserID()));
                }
            }
        }

        // Handles when user clicks on Unban User button
        public class UnbanUserButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                User selectedUser = users.getSelectedValue();
                if (selectedUser != null) {
                    client.queueMessage(new UnbanUser(selectedUser.getUserID()));
                }
            }
        }

        // Handles when user clicks on View Chat Logs button
        public class ViewChatLogButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                client.queueMessage(new AskChatBoxList());
            }
        }

        // **HideChatBoxButtonListener** inner class
        private class HideChatBoxButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                client.queueMessage(new AskChatBoxList());
            }
        }

        // **UnhideChatBoxButtonListener** inner class
        private class UnhideChatBoxButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                client.queueMessage(new AskChatBoxList());
            }
        }

        // **CreateUserButtonListener** inner class
        private class CreateUserButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                CreateUserDialog createUserDialog = new CreateUserDialog(AdminOptionsWindow.this, client);
                createUserDialog.setVisible(true);
            }
        }
    }

    // **CreateUserDialog** class
    public class CreateUserDialog extends JDialog {
        private final JTextField usernameField;
        private final JPasswordField passwordField;
        private final JCheckBox isAdminCheckBox;
        private final JButton createButton;
        private final JButton cancelButton;
        private final Client client;

        public CreateUserDialog(AdminOptionsWindow adminOptionsWindow, Client client) {
            super(adminOptionsWindow, "Create User", true);
            this.client = client;

            // Initialize components
            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            isAdminCheckBox = new JCheckBox("Is Admin");
            createButton = new JButton("Create");
            cancelButton = new JButton("Cancel");

            // Set up layout
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Username label and field
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            panel.add(usernameField, gbc);

            // Password label and field
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);

            // Is Admin checkbox
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            panel.add(isAdminCheckBox, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(createButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            panel.add(buttonPanel, gbc);

            // Add action listeners
            createButton.addActionListener(e -> createUser());
            cancelButton.addActionListener(e -> dispose());

            setContentPane(panel);
            pack();
            setLocationRelativeTo(adminOptionsWindow);
        }

        private void createUser() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            boolean isAdmin = isAdminCheckBox.isSelected();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            client.queueMessage(new CreateUser(username, password, isAdmin));
            dispose();
        }
    }

    // Custom cell renderer for User lists
    public class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            // Let the default renderer set up the label
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof User user) {
                String displayName = user.getUsername();
                if (user.isBanned()) {
                    displayName += " (banned)";
                }
                label.setText(displayName);
            }
            return label;
        }
    }
}
