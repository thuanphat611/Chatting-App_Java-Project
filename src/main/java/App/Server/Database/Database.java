package App.Server.Database;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static String user = "sa";
    private static String pass = "123456"; //change this to your password
    private static String dbPort = "1433";
    private static String databaseName = "CHATTING_APP_21127665";

    public Connection getConnection() {
        Connection conn = null;
        String dbURL = "jdbc:sqlserver://localhost:" + dbPort + ";" + "databaseName=" + databaseName + ";" + "user=" + user + ";password=" + pass;
        try {
            DriverManager.registerDriver(new SQLServerDriver());
            conn = DriverManager.getConnection(dbURL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
}
