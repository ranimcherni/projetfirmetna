
import utils.MyDataBase;
import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            if (cnx == null) {
                System.out.println("Connection is NULL");
                return;
            }
            DatabaseMetaData dbmd = cnx.getMetaData();
            ResultSet tables = dbmd.getTables(null, null, "%", new String[]{"TABLE"});
            System.out.println("Available Tables:");
            while (tables.next()) {
                System.out.println("- " + tables.getString("TABLE_NAME"));
            }
            
            Statement st = cnx.createStatement();
            // Try different case names just in case
            String[] possibleNames = {"user", "User", "USERS", "utilisateurs"};
            for (String name : possibleNames) {
                try {
                    ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + name + "`");
                    if (rs.next()) {
                        System.out.println("TABLE [" + name + "] exists and has " + rs.getInt(1) + " rows.");
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
