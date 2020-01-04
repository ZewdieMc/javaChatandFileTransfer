package connect2g;

import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import javax.swing.JOptionPane;

public class ChatServer {
    
    int portNumber = 8188;
    ServerSocket serversocket;
    BufferedReader reader;
    PrintWriter writer;
    String recieverNames[];
    private static Map<String, PrintWriter> users = new HashMap<>();
    
    private Connection conn = null;
    private PreparedStatement pst = null;
    private ResultSet rs = null;
    private boolean userTaken = false;

    public ChatServer() {
        try {
            chatDB("chatDb");
            chatTable("chatUsers");
            deleteUsers();
            whoIsRegistered();
            serversocket = new ServerSocket(portNumber);
            while (true) {
                System.out.println("Waiting..");
                new chat(serversocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void chatDB(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:C:\\Users\\Talew\\Documents\\NetBeansProjects\\zCONNECTg\\src\\connect2g\\database" + dbName;
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                System.out.println("connection to " + dbName + " established");
            }
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void chatTable(String tname) {
        String query = "create table if not exists " + tname + " (username text NOT NULL, password text NOT NULL)";
        try {
            pst = conn.prepareStatement(query);
            pst.execute();
            System.out.println(tname + " successfully created");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private boolean chatLogin(String username, String password) {
        boolean loggedIn = false;
        String sql = "select * from chatUsers where username=?";
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            rs = pst.executeQuery();
            if (rs.next()) {
                userTaken = true;

                String dusername = rs.getString("username");
                String dpassword = rs.getString("password");
                if (dusername.equals(username) && dpassword.equals(password)) {
                    loggedIn = true;
                } else {
                    System.out.println(username + " exists but can't login");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }
        return loggedIn;
    }

    private boolean chatRegister(String username, String password) {
        String qury = "insert into chatUsers(username,password) values(?,?)";
        boolean registered = false;
        if (userTaken) {
            System.out.println(username + " is already registered");
        } else {

            try {
                pst = conn.prepareStatement(qury);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.execute();
                registered = true;
            } catch (SQLException e) {
                System.out.println("error: " + e);
            }
        }
        return registered;
    }

    private void whoIsRegistered() {
        String sql = "select * from chatUsers";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("username") + " : " + rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private void deleteUsers() {
        String sql = "delete from chatUsers";
        try {
            pst = conn.prepareStatement(sql);
            pst.execute();
            System.out.println("table cleared.");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    class chat extends Thread {

        BufferedReader reader;
        PrintWriter writer;
        Socket ss;

        public chat(Socket s) {
            ss = s;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(ss.getInputStream()));
                writer = new PrintWriter(ss.getOutputStream(), true);
                writer.println("Send name");
                while (true) {
                    String name = reader.readLine();
                    if (null == name) {
                    } else if (name.startsWith("Name_")) {
                        String username = name.substring(5, name.indexOf(":"));
                        String password = name.substring(name.indexOf(":") + 1);
                        if (chatLogin(username, password)) {
                            writer.println("xyz connected");
                            users.put(username, writer);
                            String a = users.keySet().toString();
                            System.out.println("Client loggedin " + username);
                            for (PrintWriter P : users.values()) {
                                P.println("_O" + a);
                            }
                        } else if (chatRegister(username, password)) {
                            users.put(username, writer);
                            String a = users.keySet().toString();
                            System.out.println("Client registered " + username);
                            for (PrintWriter P : users.values()) {
                                P.println("_O" + a);
                            }
                        } else {
                            writer.println("xxxloginError");
                        }
                    } else if (name.startsWith("update chatUsers")) {
                        try {
                            pst = conn.prepareStatement(name);
                            pst.execute();
                        } catch (SQLException e) {
                            System.out.println("update exceprion: " + e);
                        }
                    } else if (name.startsWith("type")) {
                        int m = name.indexOf("=");
                        String y = name.substring(4, m);
                        recieverNames = y.split(", ");
                        for (String recievernam : recieverNames) {
                            if (recievernam.contains(" is typing..")) {
                                recievernam = recievernam.substring(0, recievernam.indexOf(" is typing..."));
                            }
                            PrintWriter P = users.get(recievernam);
                            P.println("type" + name.substring(m + 1));
                        }

                    } else if (name.startsWith("Message")) {
                        String y = name.substring(7, name.indexOf("="));
                        recieverNames = y.split(", ");
                        int w = name.indexOf("=");
                        for (String recievernam : recieverNames) {
                            if (recievernam.contains(" is typing..")) {
                                recievernam = recievernam.substring(0, recievernam.indexOf(" is typing..."));
                            }
                            PrintWriter P = users.get(recievernam);
                            P.println("Message" + name.substring(w));
                        }

                    } else if (name.startsWith("exit")) {

                        String clientexiting = name.substring(4);

                        users.remove(clientexiting);

                        String a = users.keySet().toString();
                        for (PrintWriter P : users.values()) {
                            P.println("_O" + a);
                        }

                    } else if (name.startsWith("focuslost")) {
                        String y = name.substring(9, name.indexOf(":"));
                        recieverNames = y.split(", ");
                        for (String recievernam : recieverNames) {
                            PrintWriter P = users.get(recievernam);
                            if (P != null) {
                                P.println("focus" + recievernam + ":" + name.substring(name.indexOf(":") + 1));
                            }
                        }

                    } else if (name.startsWith("seen")) {
                        String y = name.substring(4, name.indexOf(":"));
                        recieverNames = y.split(",");
                        for (String rName : recieverNames) {
                            PrintWriter P = users.get(rName);
                            if (P != null) {
                                P.println("seen" + rName + ":" + name.substring(name.indexOf(":") + 1));
                            }
                        }

                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(chat.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
       
    }

}
