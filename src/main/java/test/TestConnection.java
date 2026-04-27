package test;

import utils.MyDataBase;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {

        Connection conn = MyDataBase.getInstance().getCnx();

        if (conn != null) {
            System.out.println("✅ Test Passed: Connection established successfully!");
        } else {
            System.err.println("❌ Test Failed: Connection is null.");
        }
    }
}
