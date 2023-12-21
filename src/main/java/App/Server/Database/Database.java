package App.Server.Database;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private static String user = "sa";
    private static String pass = "123456"; //change this to your password
    private static String dbPort = "1433";
    private static String databaseName = "CHATTING_APP_21127665";
    private Connection conn;
    private PreparedStatement usernameCheckStmt;
    private PreparedStatement loginStmt;
    private  PreparedStatement registerStmt;
    private PreparedStatement groupNameCheckStmt;
    private PreparedStatement createGroupStmt;
    private  PreparedStatement addMemberStmt;
    private PreparedStatement getGroupStmt;
    private PreparedStatement getMsgIDStmt;
    private PreparedStatement saveMsgStmt;
    private PreparedStatement leaveGroupStmt;
    private PreparedStatement deleteGroupStmt;
    private PreparedStatement groupCheckStmt;
    private PreparedStatement groupMemberCheckStmt;
    private PreparedStatement getGroupMsgStmt;
    private PreparedStatement deleteGroupHistoryStmt;
    private PreparedStatement clearAllMessageStmt;
    private PreparedStatement deleteOneMessageStmt;

    public Database() {
        conn = null;
        String dbURL = "jdbc:sqlserver://localhost:" + dbPort + ";" + "databaseName=" + databaseName + ";encrypt=true;trustServerCertificate=true;" + "user=" + user + ";password=" + pass;
        try {
            DriverManager.registerDriver(new SQLServerDriver());
            conn = DriverManager.getConnection(dbURL);
            if (conn != null) {
                usernameCheckStmt = conn.prepareStatement("SELECT * FROM ACCOUNTS WHERE USERNAME = ?");
                loginStmt = conn.prepareStatement("SELECT * FROM ACCOUNTS WHERE USERNAME = ? AND PASSWORD = ?");
                registerStmt = conn.prepareStatement("INSERT ACCOUNTS (USERNAME, PASSWORD) VALUES (?, ?)");
                groupNameCheckStmt = conn.prepareStatement("SELECT * FROM GROUPS WHERE OWNER = ? AND GROUP_NAME = ?");
                createGroupStmt = conn.prepareStatement("INSERT GROUPS (GROUP_NAME, OWNER) VALUES (?, ?)");
                addMemberStmt = conn.prepareStatement("INSERT GROUP_MEMBERS (GROUP_NAME, OWNER, MEMBER_NAME) VALUES (?, ?, ?)");
                getGroupStmt = conn.prepareStatement("SELECT * FROM GROUP_MEMBERS WHERE MEMBER_NAME = ?");
                getMsgIDStmt = conn.prepareStatement("SELECT * FROM CHAT_HISTORY WHERE (SENDER = ? AND RECEIVER = ?) OR (SENDER = ? AND RECEIVER = ?) ORDER BY ORDER_INDEX DESC");
                saveMsgStmt = conn.prepareStatement("INSERT CHAT_HISTORY (ORDER_INDEX, SENDER, RECEIVER, CONTENT, TYPE) VALUES (?, ?, ?, ?, ?)");
                leaveGroupStmt = conn.prepareStatement("DELETE GROUP_MEMBERS WHERE GROUP_NAME = ? AND OWNER = ? AND MEMBER_NAME = ?");
                deleteGroupStmt = conn.prepareStatement("DELETE GROUPS WHERE GROUP_NAME = ? AND OWNER = ?");
                groupCheckStmt = conn.prepareStatement("SELECT * FROM GROUPS GR, GROUP_MEMBERS GM WHERE GR.GROUP_NAME = GM.GROUP_NAME AND GR.OWNER = GM.OWNER AND GR.GROUP_NAME = ? AND GR.OWNER = ?");
                groupMemberCheckStmt = conn.prepareStatement("SELECT * FROM GROUP_MEMBERS WHERE GROUP_NAME = ? AND OWNER = ? AND MEMBER_NAME = ?");
                getGroupMsgStmt = conn.prepareStatement("SELECT * FROM CHAT_HISTORY WHERE RECEIVER = ? ORDER BY ORDER_INDEX DESC");
                deleteGroupHistoryStmt = conn.prepareStatement("DELETE CHAT_HISTORY WHERE RECEIVER = ?");
                clearAllMessageStmt = conn.prepareStatement("DELETE CHAT_HISTORY WHERE SENDER = ? AND RECEIVER = ?");
                deleteOneMessageStmt = conn.prepareStatement("DELETE CHAT_HISTORY WHERE SENDER = ? AND RECEIVER = ? AND ORDER_INDEX = ?");
            }
            else {
                System.out.println("Database: error creating connection");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean usernameCheck(String username) {
        boolean result = false;
        try {
            usernameCheckStmt.setString(1, username);
            ResultSet rs = usernameCheckStmt.executeQuery();
            if (!rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public boolean login(String username, String password) {
        boolean result = false;
        try {
            loginStmt.setString(1, username);
            loginStmt.setString(2, password);
            ResultSet rs = loginStmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public boolean register(String username, String password) {
        if (!usernameCheck(username)) {
            return false;
        }
        try {
            registerStmt.setString(1, username);
            registerStmt.setString(2, password);
            registerStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    //if member is in the group -> true, else false
    public boolean groupMemberCheck(String groupName, String owner, String memberName) {
        boolean result = false;
        try {
           groupMemberCheckStmt.setString(1, groupName);
           groupMemberCheckStmt.setString(2, owner);
           groupMemberCheckStmt.setString(3, memberName);
           ResultSet rs = groupMemberCheckStmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
//TODO delete message history when the group is deleted
    public boolean groupNameCheck(String username, String groupName) {
        boolean result = false;
        try {
            groupNameCheckStmt.setString(1, username);
            groupNameCheckStmt.setString(2, groupName);
            ResultSet rs = groupNameCheckStmt.executeQuery();
            if (!rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void addMemberToGroup(String groupName, String owner, String memberName) {
        if (groupNameCheck(owner, groupName)) { //check if group exists, true -> not exists
            return;
        }
        try {
            addMemberStmt.setString(1, groupName);
            addMemberStmt.setString(2, owner);
            addMemberStmt.setString(3, memberName);
            addMemberStmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean createGroup(String username, String groupName) {
        if (!groupNameCheck(username, groupName)) {
            return false;
        }
        try {
            createGroupStmt.setString(1, groupName);
            createGroupStmt.setString(2, username);
            createGroupStmt.executeUpdate();
            addMemberToGroup(groupName,username, username);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public String[] getGroups(String username) {
        String[] result = null;
        try {
            getGroupStmt.setString(1, username);
            ResultSet rs = getGroupStmt.executeQuery();
            if (rs == null)
                return null;

            ArrayList<String> groups = new ArrayList<>();

            while (rs.next()) {
                String groupName = rs.getString("GROUP_NAME") + "|" + rs.getString("OWNER");
                groups.add(groupName);
            }
            if (groups.isEmpty())
                return null;

            result = new String[groups.size()];

            for (int i = 0; i < groups.size(); i++)
                result[i] = groups.get(i);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    // check if the group has no member. If true, delete the group
    void groupCheck(String groupName, String owner) {
        try {
            groupCheckStmt.setString(1, groupName);
            groupCheckStmt.setString(2, owner);

            ResultSet rs = groupCheckStmt.executeQuery();
            if (!rs.next())
                deleteGroup(groupName, owner);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteGroup(String groupName, String owner) {
        try {
            //delete group in table GROUPS
            deleteGroupStmt.setString(1, groupName);
            deleteGroupStmt.setString(2, owner);
            deleteGroupStmt.executeUpdate();
            //delete group chat history
            deleteGroupHistoryStmt.setString(1, owner + " " + groupName);
            deleteGroupHistoryStmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void leaveGroup(String groupName, String owner, String username) {
        try {
           leaveGroupStmt.setString(1, groupName);
           leaveGroupStmt.setString(2, owner);
           leaveGroupStmt.setString(3, username);
           leaveGroupStmt.executeUpdate();
           groupCheck(groupName, owner); // check if the group has no member. If true, delete the group
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int getMsgID(String name1, String name2) {
        try {
            getMsgIDStmt.setString(1, name1);
            getMsgIDStmt.setString(4, name1);
            getMsgIDStmt.setString(2, name2);
            getMsgIDStmt.setString(3, name2);
            ResultSet rs = getMsgIDStmt.executeQuery();
            if (!rs.next())
                return 0;
            else
                return rs.getInt("ORDER_INDEX");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public int getGroupMsgID(String owner, String groupName) {
        try {
            String receiver = owner + " " + groupName;
            getGroupMsgStmt.setString(1, receiver);
            ResultSet rs = getGroupMsgStmt.executeQuery();
            if (!rs.next())
                return 0;
            else
                return rs.getInt("ORDER_INDEX");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public void saveMsgHistory(String sender, String receiver, String content, String type) {
        try {
            int lastIndex = getMsgID(sender, receiver);
            int id = lastIndex + 1;
            saveMsgStmt.setInt(1, id);
            saveMsgStmt.setString(2, sender);
            saveMsgStmt.setString(3, receiver);
            saveMsgStmt.setString(4, content);
            saveMsgStmt.setString(5, type);
            saveMsgStmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<String[]> getAllMessages(String sender, String receiver) {
        ArrayList<String[]> result = new ArrayList<>();
        try {
            getMsgIDStmt.setString(1, sender);
            getMsgIDStmt.setString(4, sender);
            getMsgIDStmt.setString(2, receiver);
            getMsgIDStmt.setString(3, receiver);
            ResultSet rs = getMsgIDStmt.executeQuery();
            while (rs.next()) {
                String[] message = new String[3];
                message[0] = rs.getString("SENDER");
                message[1] = rs.getString("CONTENT");
                message[2] = rs.getString("TYPE");
                result.add(message);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void clearAllMessages(String sender, String receiver) {
        try {
            clearAllMessageStmt.setString(1, sender);
            clearAllMessageStmt.setString(2, receiver);
            clearAllMessageStmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<String[]> getAllGroupMessages(String owner, String groupName) {
        ArrayList<String[]> result = new ArrayList<>();
        try {
            String receiver = owner + " " + groupName;
            getGroupMsgStmt.setString(1, receiver);
            ResultSet rs = getGroupMsgStmt.executeQuery();
            while (rs.next()) {
                String[] message = new String[3];
                message[0] = rs.getString("SENDER");
                message[1] = rs.getString("CONTENT");
                message[2] = rs.getString("TYPE");
                result.add(message);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void saveGroupMsgHistory(String sender, String owner, String groupName, String content, String type) {
        try {
            int lastIndex = getGroupMsgID(owner, groupName);
            String receiver = owner + " " + groupName;
            int id = lastIndex + 1;
            saveMsgStmt.setInt(1, id);
            saveMsgStmt.setString(2, sender);
            saveMsgStmt.setString(3, receiver);
            saveMsgStmt.setString(4, content);
            saveMsgStmt.setString(5, type);
            saveMsgStmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //test Database's method todo delete this
    public static void main(String[] args) {
        Database db1 = new Database();
//        String[] res = db1.getGroups("admin");
//        for (String i : res)
//            System.out.println(i);
//        System.out.println(db1.register("admin", "123456"));
//        db1.createGroup("admin", "test group 2");
//        System.out.println(db1.groupNameCheck("admin", "test group"));
//        db1.saveMsgHistory("phat", "admin", "hello");
//        System.out.println(db1.checkMsgID("admin_phat_001"));

//        db1.leaveGroup("test group", "admin", "admin");
//        db1.deleteGroup("test group", "admin");
//        db1.groupCheck("test group", "admin");
//        System.out.println(db1.getMsgID("admin", "phat"));
//        ArrayList<String[]> result = db1.getAllGroupMessages("admin", "test group");
//        for (int i = 0; i < result.size(); i++)
//            System.out.println(result.get(i)[0] + " " + result.get(i)[1]);
    }
}