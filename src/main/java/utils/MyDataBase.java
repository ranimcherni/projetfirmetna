package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL = "jdbc:mysql://localhost:3306/firmetna_new_db";
    private final String USER = "root";
    private final String PASSWORD = "";

    private static MyDataBase instance;
    private Connection cnx;

    private MyDataBase() {
        try {
            // Chargement explicite du pilote (peut aider dans certains environnements)
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("--- Connection Successful to firmetna_db! ---");
        } catch (ClassNotFoundException e) {
            System.err.println("--- MySQL Driver not found! Check your pom.xml ---");
        } catch (SQLException e) {
            System.err.println("--- Connection Failed! Check if MySQL is running on port 3307 ---");
            System.err.println("Error details: " + e.getMessage());
            if (e.getMessage().contains("Unknown database")) {
                System.err.println("TIP: Ensure the database 'firmetna_new_db' exists in your MySQL server.");
            }
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
