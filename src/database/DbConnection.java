package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bancadb" + "?useSSL=false" + "&allowPublicKeyRetrieval=true" + "&serverTimezone=UTC";
    private static final String USER = "rares";
    private static final String PASSWORD = "root";

    private DbConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
