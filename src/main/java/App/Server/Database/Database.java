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
    private PreparedStatement checkMsgIDStmt;
    private PreparedStatement saveMsgStmt;
    private PreparedStatement leaveGroupStmt;
    private PreparedStatement deleteGroupStmt;
    private PreparedStatement groupCheckStmt;

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
                checkMsgIDStmt = conn.prepareStatement("SELECT * FROM CHAT_HISTORY WHERE ID = ?");
                saveMsgStmt = conn.prepareStatement("INSERT CHAT_HISTORY (ID, SENDER, RECEIVER, CONTENT) VALUES (?, ?, ?, ?)");
                leaveGroupStmt = conn.prepareStatement("DELETE GROUP_MEMBERS WHERE GROUP_NAME = ? AND OWNER = ? AND MEMBER_NAME = ?");
                deleteGroupStmt = conn.prepareStatement("DELETE GROUPS WHERE GROUP_NAME = ? AND OWNER = ?");
                groupCheckStmt = conn.prepareStatement("SELECT * FROM GROUPS GR, GROUP_MEMBERS GM WHERE GR.GROUP_NAME = GM.GROUP_NAME AND GR.OWNER = GM.OWNER AND GR.GROUP_NAME = ? AND GR.OWNER = ?");
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
                String groupName = rs.getString("GROUP_NAME");
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
            deleteGroupStmt.setString(1, groupName);
            deleteGroupStmt.setString(2, owner);
            deleteGroupStmt.executeUpdate();
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

    public boolean checkMsgID(String id) {
        boolean result = false;
        try {
            checkMsgIDStmt.setString(1, id);
            ResultSet rs = checkMsgIDStmt.executeQuery();
            if (!rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void saveMsgHistory(String id ,String sender, String receiver, String content) {
        try {
            if (!checkMsgID(id)) {
                System.out.println("Can not save message history due to id duplication");
                return;
            }
            saveMsgStmt.setString(1, id);
            saveMsgStmt.setString(2, sender);
            saveMsgStmt.setString(3, receiver);
            saveMsgStmt.setString(4, content);
            saveMsgStmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //test Database's method
    public static void main(String[] args) {
        Database db1 = new Database();
//        String[] res = db1.getGroups("admin");
//        for (String i : res)
//            System.out.println(i);
//        System.out.println(db1.register("admin", "123456"));
//        db1.createGroup("admin", "test group");
//        System.out.println(db1.groupNameCheck("admin", "test group"));
//        db1.saveMsgHistory("admin_phat_001", "admin", "phat", "hello");
//        System.out.println(db1.checkMsgID("admin_phat_001"));

//        db1.leaveGroup("test group", "admin", "admin");
//        db1.deleteGroup("test group", "admin");
//        db1.groupCheck("test group", "admin");
    }
}