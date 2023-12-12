package App.Server.Database;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

import java.sql.*;

public class Database {
    private static String user = "sa";
    private static String pass = "123456"; //change this to your password
    private static String dbPort = "1433";
    private static String databaseName = "CHATTING_APP_21127665";
    private Connection conn;
    private PreparedStatement usernameCheckStmt;
    private PreparedStatement loginStmt;
    private  PreparedStatement registerStmt;

    public Database() {
        conn = null;
        String dbURL = "jdbc:sqlserver://localhost:" + dbPort + ";" + "databaseName=" + databaseName + ";encrypt=true;trustServerCertificate=true;" + "user=" + user + ";password=" + pass;
        try {
            DriverManager.registerDriver(new SQLServerDriver());
            conn = DriverManager.getConnection(dbURL);

            usernameCheckStmt = conn.prepareStatement("SELECT * FROM ACCOUNTS WHERE USERNAME = ?");
            loginStmt = conn.prepareStatement("SELECT * FROM ACCOUNTS WHERE USERNAME = ? AND PASSWORD = ?");
            registerStmt = conn.prepareStatement("INSERT ACCOUNTS (USERNAME, PASSWORD) VALUES (?, ?)");
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
    public static void main(String[] args) {
        Database db1 = new Database();
        System.out.println(db1.register("admin", "123456"));
    }
}
