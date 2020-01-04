package connect2g;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.swing.DefaultListModel;
import java.awt.event.MouseAdapter;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client extends javax.swing.JFrame {

    public static String recieverAddress = "";
    String clientName;
    String recieverName;
    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    int portNumber = 8188;
    String onlUsers[];
    String filteredList;
    boolean windowActive = false;
    private static Thread t;
    private int inboxCounter = 0;
    String serverName;
    DefaultListModel model;
    static BufferedImage image = null;
    Graphics g;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    static SimpleAttributeSet sentMsgColor = new SimpleAttributeSet();
    static SimpleAttributeSet receivedMsgColor = new SimpleAttributeSet();
    static SimpleAttributeSet receiverNameColor = new SimpleAttributeSet();
    static SimpleAttributeSet senderNameColor = new SimpleAttributeSet();
    static SimpleAttributeSet fromStyle = new SimpleAttributeSet();
    static SimpleAttributeSet seenStyle = new SimpleAttributeSet();

    Icon iconGeni = new ImageIcon(getClass().getResource("/connect2g/enat.png"));
    Icon iconZed = new ImageIcon(getClass().getResource("/connect2g/zed.png"));
    Icon iconSeen = new ImageIcon(getClass().getResource("/connect2g/check.png"));

    private boolean inboxReceived = false;
    private int mouseClicked = 0;
    private boolean isRegister = false;

    public Client() {

        StyleConstants.setForeground(seenStyle, Color.gray.darker());
        StyleConstants.setBold(seenStyle, true);
        StyleConstants.setFontSize(seenStyle, 9);
        StyleConstants.setIcon(fromStyle, iconSeen);

        StyleConstants.setForeground(sentMsgColor, Color.green.darker());
        StyleConstants.setBold(sentMsgColor, true);
        StyleConstants.setFontSize(sentMsgColor, 16);
        StyleConstants.setFontFamily(sentMsgColor, "Nyala");

        StyleConstants.setForeground(receivedMsgColor, Color.magenta.darker());
        StyleConstants.setBold(receivedMsgColor, true);
        StyleConstants.setFontSize(receivedMsgColor, 16);
        StyleConstants.setFontFamily(receivedMsgColor, "Nyala");

        StyleConstants.setForeground(receiverNameColor, Color.white);
        StyleConstants.setBold(receiverNameColor, true);
        StyleConstants.setFontSize(receiverNameColor, 14);
        StyleConstants.setFontFamily(receiverNameColor, "sans serif");
        StyleConstants.setBackground(receiverNameColor, Color.green.darker().darker());
        StyleConstants.setIcon(receiverNameColor, iconGeni);
        StyleConstants.setAlignment(receiverNameColor, StyleConstants.ALIGN_RIGHT);

        StyleConstants.setForeground(senderNameColor, Color.yellow);
        StyleConstants.setBold(senderNameColor, true);
        StyleConstants.setFontSize(senderNameColor, 14);
        StyleConstants.setFontFamily(senderNameColor, "sans serif");
        StyleConstants.setBackground(senderNameColor, Color.green.darker().darker());
        StyleConstants.setIcon(senderNameColor, iconZed);
        StyleConstants.setAlignment(receiverNameColor, StyleConstants.ALIGN_RIGHT);

        javax.swing.UIManager.put("ProgressBar.foreground", Color.GREEN.darker().darker().darker());
        javax.swing.UIManager.put("ProgressBar.selectionForeground", Color.white);

        jScrollPane4 = new JScrollPane(tPane);
        initialize();
        initComponents();
//        changePassword.setVisible(false);
        progressBar.setVisible(false);
        openFolder.setVisible(false);
        inboxCount.setVisible(false);
        model = new DefaultListModel();
        list.setModel(model);
        list.setSelectionBackground(Color.green.darker());
        list.setForeground(Color.yellow.darker());
        list.setBackground(Color.gray.darker().darker());
        list.setFont(new Font("", Font.BOLD, 14));

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filteredList = recieverName();
                enableChat();
            }
        }
        );
        disableChat();
    }

    private void connect() {
        serverName = serveraddress.getText();
        progressBar.setVisible(false);
        Thread thread = new Thread(new Thread() {
            @Override
            public void run() {
                try {
                    connecting.setForeground(Color.yellow.darker());
                    connecting.setText("connecting to " + serverName);
                    socket = new Socket(serverName, portNumber);
                    connecting.setForeground(Color.green.darker().darker());
                    if(!(password.getText().equals("")||username.getText().equals(""))){
                    connect.setVisible(false);
                    serveraddress.setEnabled(false);
                    userName.setEnabled(false);
                    password.setEnabled(false);
                    }
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    while (true) {
                        String x = reader.readLine();
                        if (x == null) {
                        } else {

                            if (x.startsWith("Send name")) {
                                clientName = userName.getText();//System.getProperty("user.name");
                                if (!"".equals(clientName)) {
                                    writer.println("Name_" + clientName + ":" + new String(password.getPassword()));
                                } else {
                                    connecting.setText("<html><font color = \"red\">password and/or username can't be empty</font></html>");
                                }
                            } else if (x.startsWith("xyz connected")) {
                                connecting.setText("connected to " + InetAddress.getByName(serverName).getHostName());
//                                changePassword.setVisible(false);
                                setTitle(clientName);
                                
                            } else if (x.startsWith("_O")) {
                                String y = x.substring(3, x.length() - 1);
                                onlUsers = y.split(", ");
                                int i = 0;
                                model.removeAllElements();
                                for (String name : onlUsers) {
                                    model.add(i, name);
                                    i++;
                                }

                            } else if (x.startsWith("xxxloginError")) {
                                connecting.setText("<html><font color=\"red\"> Username and/or Password is/are incorrect</font><html>");
               //                 changePassword.setVisible(true);
                                userName.setEnabled(true);
                                password.setEnabled(true);
                                connect.setVisible(true);
                            } else if (x.startsWith("type")) {
                                String a = x.substring(4);

                                if (!a.startsWith(clientName)) {
                                    String listName = a;
                                    int index = 0;
                                    if (model.contains(listName)) {
                                        index = model.indexOf(listName);
                                        model.set(index, listName + " is typing...");
                                    } else if (model.contains(listName + "(" + inboxCounter + ")")) {
                                        index = model.indexOf(listName + "(" + inboxCounter + ")");
                                        model.set(index, listName + " is typing...");
                                    }
                                } else {
                                }

                            } else if (x.startsWith("seen")) {
                                String m = x.substring(4);
                                if (!m.startsWith(clientName)) {

                                } else {
                                    if (!"".equals(tPane.getText())) {
                                        Calendar cal = Calendar.getInstance();

                                        try {
                                            String text = tPane.getDocument().getText(0, tPane.getDocument().getLength());
                                            int lastLineBreak = text.lastIndexOf("\n");
                                            if (lastLineBreak == -1) {
                                                lastLineBreak = 0;
                                            }
                                            tPane.getDocument().remove(lastLineBreak, tPane.getDocument().getLength() - lastLineBreak);
                                        } catch (BadLocationException e) {
                                            System.out.println(e);
                                        }
                                        addText("\tseen by " + m.substring(m.indexOf(":") + 1) + "(" + sdf.format(cal.getTime()) + ")", fromStyle);
                                        addText("seen by " + m.substring(m.indexOf(":") + 1) + "(" + sdf.format(cal.getTime()) + ")", seenStyle);
                                    }
                                }

                            } else if (x.startsWith("Message")) {
                                String a = x.substring(8, x.length());
                                if (!a.startsWith(clientName)) {
                                    inboxCount.setVisible(true);
                                    String listName = a.substring(0, a.indexOf(":"));
                                    inboxCounter++;
                                    inboxCount.setText(inboxCounter + "");
                                    int position = 0;
                                    if (model.contains(listName + " is typing...")) {
                                        position = model.indexOf(listName + " is typing...");
                                        model.set(position, listName + "(" + inboxCounter + ")");
                                    } else if (model.contains(listName + "(" + Integer.toString(inboxCounter - 1) + ")")) {
                                        position = model.indexOf(listName + "(" + Integer.toString(inboxCounter - 1) + ")");
                                        model.set(position, listName + "(" + inboxCounter + ")");
                                    }

                                    try {
                                        String text = tPane.getDocument().getText(0, tPane.getDocument().getLength());
                                        int lastLineBreak = text.lastIndexOf("\n");
                                        String[] arr = text.split("\n");
                                        if (lastLineBreak == -1) {
                                            lastLineBreak = 0;
                                        }
                                        if (arr[arr.length - 1].startsWith("\tseen")) {
                                            tPane.getDocument().remove(lastLineBreak, tPane.getDocument().getLength() - lastLineBreak);
                                        }
                                    } catch (BadLocationException e) {
                                        System.out.println(e);
                                    }
                                    addText("<-", senderNameColor);
                                    addText(a.substring(a.indexOf(":") + 1), receivedMsgColor);
                                    addText(" (from " + listName + ")\n\n\n\n\n", seenStyle);

                                    tPane.setCaretPosition(tPane.getDocument().getLength());
                                    inboxReceived = true;
                                    try {
                                        chatSound();
                                    } catch (Exception ex) {
                                    }
                                }

                            } else if (x.startsWith("focus")) {
                                String a = x.substring(5);
                                if (!a.startsWith(clientName)) {
                                } else {
                                    String listName = a.substring(a.indexOf(":") + 1);
                                    if (model.indexOf(listName + " is typing...") != -1) {
                                        model.set(model.indexOf(listName + " is typing..."), listName);
                                    }
                                }
                            }

                        }
                    }
                } catch (IOException ex) {
                    connecting.setForeground(Color.red);
                    connect.setVisible(true);
                    connect.setText("Try again");
                    connecting.setText("<html><strong><font color=\"red\">Connection to " + serveraddress.getText() + " failed. Server might be down.</font>"
                            + " Go to <font color=\"green\">Option</font>-><font color = \"green\"><br> startLocalServer</font>"
                            + " or Enter Correct <font color = \"green\">Server Address</font> and Reconnect</strong></html>");
                    serveraddress.setEnabled(true);
                }
            }
        }
        );
        thread.start();
    }

    private void addText(String text, AttributeSet set) {
        try {
            StyledDocument doc = tPane.getStyledDocument();
            tPane.getStyledDocument().insertString(doc.getLength(), text, set);
        } catch (BadLocationException e) {
            System.out.println("document error: " + e);
        }
    }

    private void enableChat() {
        tPane.setEnabled(true);
        typeArea.setEditable(true);
        Send.setEnabled(true);
    }

    private void disableChat() {
        tPane.setEnabled(false);
        typeArea.setEditable(false);
        Send.setEnabled(false);
    }

    private void initialize() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("talk5.png")));
    }

    public static void chatSound() throws Exception {
        URL path = Client.class.getResource("chat.wav");
        try {
            Clip sondio = AudioSystem.getClip();
            sondio.open(AudioSystem.getAudioInputStream(path));
            sondio.start();
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu2 = new javax.swing.JPopupMenu();
        ClearChat = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        inboxCount = new javax.swing.JLabel();
        connect = new javax.swing.JButton();
        username = new javax.swing.JLabel();
        userName = new javax.swing.JTextField();
        username1 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        connecting = new javax.swing.JLabel();
        username2 = new javax.swing.JLabel();
        serveraddress = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        typeArea = new javax.swing.JTextField();
        Send = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tPane = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        sendFile = new javax.swing.JButton();
        recieveFile = new javax.swing.JButton();
        ready = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        openFolder = new javax.swing.JButton();
        timecounter = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        IfColor = new javax.swing.JMenuItem();
        OfColor = new javax.swing.JMenuItem();
        NBColor = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        ClearChat.setText("Clear Conversation");
        ClearChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearChatActionPerformed(evt);
            }
        });
        jPopupMenu2.add(ClearChat);

        Exit.setText("jMenuItem4");
        jPopupMenu2.add(Exit);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 0, 0));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane3.setBackground(new java.awt.Color(178, 9, 38));
        jTabbedPane3.setForeground(new java.awt.Color(247, 245, 245));

        inboxCount.setBackground(new java.awt.Color(0, 153, 0));
        inboxCount.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        inboxCount.setForeground(new java.awt.Color(255, 255, 0));
        inboxCount.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        inboxCount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/connect2g/inbox.png"))); // NOI18N
        inboxCount.setText("2");
        inboxCount.setFocusable(false);
        inboxCount.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        connect.setBackground(new java.awt.Color(0, 102, 0));
        connect.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        connect.setForeground(new java.awt.Color(255, 255, 255));
        connect.setText("Login");
        connect.setFocusable(false);
        connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectActionPerformed(evt);
            }
        });

        username.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        username.setForeground(new java.awt.Color(0, 102, 0));
        username.setText("Username:");

        userName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        userName.setForeground(new java.awt.Color(0, 102, 0));

        username1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        username1.setForeground(new java.awt.Color(0, 102, 0));
        username1.setText("password:");

        password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });

        connecting.setFont(new java.awt.Font("Times New Roman", 1, 12)); // NOI18N
        connecting.setForeground(new java.awt.Color(0, 102, 0));

        username2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        username2.setForeground(new java.awt.Color(0, 102, 0));
        username2.setText("Sever address:");

        serveraddress.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        serveraddress.setForeground(new java.awt.Color(0, 102, 0));

        typeArea.setFont(new java.awt.Font("Nyala", 1, 16)); // NOI18N
        typeArea.setForeground(new java.awt.Color(0, 0, 255));
        typeArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                typeAreaMouseClicked(evt);
            }
        });
        typeArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeAreaActionPerformed(evt);
            }
        });
        typeArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                typeAreaKeyReleased(evt);
            }
        });

        Send.setBackground(new java.awt.Color(0, 102, 0));
        Send.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        Send.setForeground(new java.awt.Color(255, 255, 255));
        Send.setText("Send");
        Send.setFocusable(false);
        Send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendActionPerformed(evt);
            }
        });

        list.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Online Users", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Times New Roman", 1, 18), new java.awt.Color(255, 204, 0))); // NOI18N
        list.setFont(new java.awt.Font("Nyala", 1, 16)); // NOI18N
        list.setForeground(new java.awt.Color(255, 204, 0));
        list.setSelectionForeground(new java.awt.Color(255, 255, 51));
        jScrollPane4.setViewportView(list);

        tPane.setEditable(false);
        tPane.setBackground(new java.awt.Color(204, 255, 255));
        tPane.setMaximumSize(new java.awt.Dimension(6, 20));
        tPane.setRequestFocusEnabled(false);
        tPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tPaneMouseReleased(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tPaneMouseEntered(evt);
            }
        });
        jScrollPane5.setViewportView(tPane);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(51, 153, 0));
        jLabel3.setText("Message");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addGap(5, 5, 5)
                        .addComponent(typeArea, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Send, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(typeArea, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Send, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(username2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serveraddress, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(username)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(connecting)
                        .addGap(44, 44, 44)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(username1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connect))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(inboxCount, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(145, 145, 145)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connecting, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 50, Short.MAX_VALUE)
                        .addComponent(inboxCount)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(username1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connect)
                    .addComponent(username2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serveraddress, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane3.addTab("Chat", jPanel3);

        sendFile.setBackground(new java.awt.Color(0, 102, 0));
        sendFile.setFont(new java.awt.Font("Times New Roman", 0, 36)); // NOI18N
        sendFile.setForeground(new java.awt.Color(255, 255, 255));
        sendFile.setText("Send");
        sendFile.setFocusable(false);
        sendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendFileActionPerformed(evt);
            }
        });

        recieveFile.setBackground(new java.awt.Color(0, 51, 204));
        recieveFile.setFont(new java.awt.Font("Times New Roman", 0, 36)); // NOI18N
        recieveFile.setForeground(new java.awt.Color(255, 255, 255));
        recieveFile.setText("Recieve");
        recieveFile.setFocusable(false);
        recieveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recieveFileActionPerformed(evt);
            }
        });

        ready.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        ready.setForeground(new java.awt.Color(0, 51, 255));

        progressBar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        progressBar.setBorderPainted(false);
        progressBar.setFocusable(false);
        progressBar.setStringPainted(true);
        progressBar.setVerifyInputWhenFocusTarget(false);

        openFolder.setBackground(new java.awt.Color(0, 102, 0));
        openFolder.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        openFolder.setForeground(new java.awt.Color(255, 255, 255));
        openFolder.setText("Open Folder");
        openFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(1, 325, Short.MAX_VALUE)
                .addComponent(timecounter, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(295, 295, 295))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(recieveFile, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                        .addComponent(sendFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(openFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ready, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 135, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(sendFile, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(recieveFile, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 194, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(openFolder))
                    .addComponent(ready, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(37, 37, 37)
                .addComponent(timecounter, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(53, 53, 53))
        );

        jTabbedPane3.addTab("FileTransfer ", jPanel2);

        jMenu1.setBackground(new java.awt.Color(204, 204, 255));
        jMenu1.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jMenu1.setForeground(new java.awt.Color(0, 153, 0));
        jMenu1.setText("Option");
        jMenu1.setFont(new java.awt.Font("Georgia", 1, 14)); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setBackground(new java.awt.Color(0, 0, 0));
        jMenuItem1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jMenuItem1.setForeground(new java.awt.Color(255, 255, 255));
        jMenuItem1.setText("Start Local Server");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        IfColor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        IfColor.setBackground(new java.awt.Color(0, 0, 0));
        IfColor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        IfColor.setForeground(new java.awt.Color(255, 255, 255));
        IfColor.setText("Change Inbox Foreground Color");
        IfColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IfColorActionPerformed(evt);
            }
        });
        jMenu1.add(IfColor);

        OfColor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        OfColor.setBackground(new java.awt.Color(0, 0, 0));
        OfColor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        OfColor.setForeground(new java.awt.Color(255, 255, 255));
        OfColor.setText("Change Outbox Foreground Color");
        OfColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OfColorActionPerformed(evt);
            }
        });
        jMenu1.add(OfColor);

        NBColor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        NBColor.setBackground(new java.awt.Color(0, 0, 0));
        NBColor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NBColor.setForeground(new java.awt.Color(255, 255, 255));
        NBColor.setText("Name Background Color");
        NBColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NBColorActionPerformed(evt);
            }
        });
        jMenu1.add(NBColor);

        jMenuBar1.add(jMenu1);

        jMenu2.setForeground(new java.awt.Color(0, 153, 0));
        jMenu2.setText("About");
        jMenu2.setFont(new java.awt.Font("Georgia", 1, 14)); // NOI18N
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 821, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void typeAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeAreaActionPerformed
        Send.doClick();
    }//GEN-LAST:event_typeAreaActionPerformed

    private void sendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendFileActionPerformed
        recieverAddress = JOptionPane.showInputDialog("<html><font color =\"blue\" ><strong><b>Enter the Ip Address of Reciever<b></strong></font></html>", "192.168.173.x");
        if (recieverAddress != null) {
            TcpDataClient.main(null);
        }
    }//GEN-LAST:event_sendFileActionPerformed
    Thread startTcpDataServer = new Thread() {
        @Override
        public void run() {
            try {
                TcpDataServer.main(null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    };
    private void recieveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recieveFileActionPerformed
        //make directory if not exists called zedShare in /home/mc/
        openFolder.setVisible(false);
        startTcpDataServer.start();
    }//GEN-LAST:event_recieveFileActionPerformed

    private void SendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendActionPerformed
        if (socket != null) {
            try {
                String text = tPane.getDocument().getText(0, tPane.getDocument().getLength());
                int lastLineBreak = text.lastIndexOf("\n");
                String[] arr = text.split("\n");
                if (lastLineBreak == -1) {
                    lastLineBreak = 0;
                }
                if (arr[arr.length - 1].startsWith("\tseen")) {
                    tPane.getDocument().remove(lastLineBreak, tPane.getDocument().getLength() - lastLineBreak);
                }
            } catch (BadLocationException e) {
                System.out.println(e);
            }
            if (typeArea.getText() != null && !typeArea.getText().equals("")) {
                writer.println("Message" + filteredList + "=" + clientName + ": " + typeArea.getText());
                addText("->", receiverNameColor);
                addText(typeArea.getText(), sentMsgColor);
                addText(" (to " + filteredList + ")\n\n\n", seenStyle);
                tPane.setCaretPosition(tPane.getDocument().getLength());
                typeArea.setText("");
            }
        }
    }//GEN-LAST:event_SendActionPerformed

    private void typeAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_typeAreaKeyReleased
        if (socket != null) {
            if (evt.getKeyCode() != 10) {
                writer.println("type" + filteredList + "=" + clientName);
            }
        }
    }//GEN-LAST:event_typeAreaKeyReleased

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (socket != null) {
            writer.println("exit" + clientName);
        }
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        if (socket != null) {
            writer.println("focuslost" + recieverName() + ":" + clientName);
        }
    }//GEN-LAST:event_formWindowLostFocus

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        connect();
        
    }//GEN-LAST:event_connectActionPerformed

    private void openFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFolderActionPerformed
        File file = new File("/home/" + System.getProperty("user.name") + "/Downloads/zedShare");
        if (file.mkdir()) {
            System.out.println("folder created");
        }
        Desktop desktop = Desktop.getDesktop();
        try {
            //Runtime.getRuntime().exec("C:\\Users\\" + System.getProperty("user.name") + "\\Downloads\\CONNECT2g\\");
            desktop.open(file);
        } catch (IOException e) {
            System.out.println("folder can't be openned");
        }
    }//GEN-LAST:event_openFolderActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        startServer.start();
        connecting.setForeground(Color.yellow.darker());
        serverName = "localhost";
        serveraddress.setText(serverName);
        connecting.setText(serverName+ " is up and ready");
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void tPaneMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tPaneMouseReleased
        if (evt.isPopupTrigger()) {
            jPopupMenu2.show(this, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_tPaneMouseReleased

    private void ClearChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearChatActionPerformed
        tPane.setText(null);
    }//GEN-LAST:event_ClearChatActionPerformed

    private void IfColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IfColorActionPerformed
        Color color = JColorChooser.showDialog(this, "choose color", Color.yellow);
        StyledDocument doc = tPane.getStyledDocument();
        StyleConstants.setForeground(receivedMsgColor, color);
    }//GEN-LAST:event_IfColorActionPerformed

    private void OfColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OfColorActionPerformed
        Color color = JColorChooser.showDialog(this, "choose color", Color.yellow);
        StyleConstants.setForeground(sentMsgColor, color);
    }//GEN-LAST:event_OfColorActionPerformed

    private void NBColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NBColorActionPerformed
        Color color = JColorChooser.showDialog(this, "choose color", Color.yellow);
        StyleConstants.setBackground(receiverNameColor, color);
        StyleConstants.setBackground(senderNameColor, color);

    }//GEN-LAST:event_NBColorActionPerformed

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        connect.doClick();
    }//GEN-LAST:event_passwordActionPerformed

    private void typeAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_typeAreaMouseClicked
        String elemnt = (String) model.elementAt(list.getSelectedIndex());
        if (elemnt.contains("(")) {
            model.set(list.getSelectedIndex(), elemnt.substring(0, elemnt.indexOf("(")));
        }
        inboxCounter = 0;
        inboxCount.setVisible(false);
        mouseClicked++;
        if (mouseClicked == 1 && inboxReceived) {
            writer.println("seen" + filteredList + ":" + clientName);
            mouseClicked = 0;
            inboxReceived = false;
        } else if (mouseClicked > 0) {
               inboxReceived = false;
         mouseClicked = 0;
            inboxReceived = false;
        }
    }//GEN-LAST:event_typeAreaMouseClicked

    private void tPaneMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tPaneMouseEntered


    }//GEN-LAST:event_tPaneMouseEntered
    Thread startServer = new Thread() {
        @Override
        public void run() {
            try {
                ChatServer.main(null);
                JOptionPane.showMessageDialog(null, "local server started");
            } catch (HeadlessException e) {
                JOptionPane.showMessageDialog(null, e);
            }
        }
    };

    private String recieverName() {
        String listOfSelectedUser = list.getSelectedValuesList().toString();
        filteredList = listOfSelectedUser.substring(1, listOfSelectedUser.length() - 1);
        String[] flist = filteredList.split(",");
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].contains("(")) {
                flist[i] = flist[i].substring(0, flist[i].indexOf("("));
            }
        }
        filteredList = Arrays.toString(flist);
        filteredList = filteredList.substring(1, filteredList.length() - 1);
        return filteredList;
    }

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Classic".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {

        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Client c = new Client();
                c.setVisible(true);
                c.setResizable(false);

            }
        });

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem ClearChat;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JMenuItem IfColor;
    private javax.swing.JMenuItem NBColor;
    private javax.swing.JMenuItem OfColor;
    private javax.swing.JButton Send;
    private javax.swing.JButton connect;
    private javax.swing.JLabel connecting;
    private javax.swing.JLabel inboxCount;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JList list;
    public static javax.swing.JButton openFolder;
    private javax.swing.JPasswordField password;
    public static javax.swing.JProgressBar progressBar;
    public static javax.swing.JLabel ready;
    private javax.swing.JButton recieveFile;
    private javax.swing.JButton sendFile;
    private javax.swing.JTextField serveraddress;
    private static javax.swing.JTextPane tPane;
    public static javax.swing.JLabel timecounter;
    private javax.swing.JTextField typeArea;
    private javax.swing.JTextField userName;
    private javax.swing.JLabel username;
    private javax.swing.JLabel username1;
    private javax.swing.JLabel username2;
    // End of variables declaration//GEN-END:variables

}
