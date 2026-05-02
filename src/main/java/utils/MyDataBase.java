package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL = "jdbc:mysql://localhost:3307/firmetna_new_db";
    private final String USER = "root";
    private final String PASSWORD = "";

    private static MyDataBase instance;
    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("--- Connection Successful to firmetna_db! ---");
        } catch (SQLException e) {
            System.err.println("--- Connection Failed! Check if MySQL is running on port 3307 ---");
            System.err.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
