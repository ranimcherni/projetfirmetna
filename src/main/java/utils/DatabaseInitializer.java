package utils;

import java.sql.*;

public class DatabaseInitializer {

    private static boolean initialized = false;

    public static synchronized void initialize() {
        if (initialized) return;

        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("Cannot initialize database: Connection is null");
            return;
        }

        try {
            createTables(cnx);
            ensureDefaultUser(cnx);
            System.out.println("--- Database Initialization/Repair Complete ---");
            initialized = true;
        } catch (SQLException e) {
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }

    private static void createTables(Connection cnx) throws SQLException {
        Statement stm = cnx.createStatement();
        DatabaseMetaData md = cnx.getMetaData();

        // 1. Create Publication Table
        String createPublication = "CREATE TABLE IF NOT EXISTS `publication` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`titre` VARCHAR(255) NOT NULL, " +
                "`contenu` TEXT NOT NULL, " +
                "`type` VARCHAR(50) DEFAULT 'Discussion', " +
                "`date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "`auteur_id` INT, " +
                "`image_path` VARCHAR(255), " +
                "`pdf_path` VARCHAR(255)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        stm.execute(createPublication);

        // Ensure date_creation, type, image_path, and pdf_path columns exist even if table was created previously
        String[] columns = {"date_creation", "type", "image_path", "pdf_path"};
        String[] definitions = {
            "ADD `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "ADD `type` VARCHAR(50) DEFAULT 'Discussion'",
            "ADD `image_path` VARCHAR(255)",
            "ADD `pdf_path` VARCHAR(255)"
        };

        for (int i = 0; i < columns.length; i++) {
            try {
                ResultSet rs = md.getColumns(null, null, "publication", columns[i]);
                if (!rs.next()) {
                    System.out.println("--- Adding missing column '" + columns[i] + "' to 'publication' table ---");
                    stm.execute("ALTER TABLE `publication` " + definitions[i]);
                } else if (columns[i].equals("date_creation")) {
                    // Special check for existing date_creation to ensure it has a default
                    String def = rs.getString("COLUMN_DEF");
                    if (def == null || def.isEmpty()) {
                        System.out.println("--- Fixing missing default for 'date_creation' in 'publication' table ---");
                        stm.execute("ALTER TABLE `publication` MODIFY `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Column '" + columns[i] + "' check/migration failed: " + e.getMessage());
            }
        }

        // 2. Create Commentaire Table
        String createCommentaire = "CREATE TABLE IF NOT EXISTS `commentaire` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`contenu` TEXT NOT NULL, " +
                "`date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "`auteur_id` INT, " +
                "`publication_id` INT, " +
                "`parent_id` INT NULL, " +
                "FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        stm.execute(createCommentaire);
        
        // Ensure necessary columns exist in commentaire
        String[] commColumns = {"date_creation", "auteur_id", "publication_id", "parent_id"};
        String[] commDefinitions = {
            "ADD `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "ADD `auteur_id` INT",
            "ADD `publication_id` INT",
            "ADD `parent_id` INT NULL"
        };

        for (int i = 0; i < commColumns.length; i++) {
            try {
                ResultSet rs = md.getColumns(null, null, "commentaire", commColumns[i]);
                if (!rs.next()) {
                    System.out.println("--- Adding missing column '" + commColumns[i] + "' to 'commentaire' table ---");
                    stm.execute("ALTER TABLE `commentaire` " + commDefinitions[i]);
                } else if (commColumns[i].equals("date_creation")) {
                    String def = rs.getString("COLUMN_DEF");
                    if (def == null || def.isEmpty()) {
                        System.out.println("--- Fixing missing default for 'date_creation' in 'commentaire' table ---");
                        stm.execute("ALTER TABLE `commentaire` MODIFY `date_creation` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Column '" + commColumns[i] + "' check/migration failed in commentaire: " + e.getMessage());
            }
        }

        // 3. Create Reaction Table
        String createReaction = "CREATE TABLE IF NOT EXISTS `reaction` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`user_id` INT NOT NULL, " +
                "`target_type` ENUM('publication','commentaire') NOT NULL, " +
                "`target_id` INT NOT NULL, " +
                "`type` ENUM('like','dislike') NOT NULL, " +
                "UNIQUE KEY unique_reaction (user_id, target_type, target_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        stm.execute(createReaction);
    }

    private static void ensureDefaultUser(Connection cnx) throws SQLException {
        // First check if 'user' table exists
        DatabaseMetaData md = cnx.getMetaData();
        ResultSet tables = md.getTables(null, null, "user", null);
        if (!tables.next()) {
            System.out.println("--- Creating 'user' table ---");
            String createUser = "CREATE TABLE `user` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                    "`email` VARCHAR(255) UNIQUE NOT NULL, " +
                    "`password` VARCHAR(255) NOT NULL, " +
                    "`role` VARCHAR(50), " +
                    "`nom` VARCHAR(100), " +
                    "`prenom` VARCHAR(100), " +
                    "`status` VARCHAR(20) DEFAULT 'Actif', " +
                    "`registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            cnx.createStatement().execute(createUser);
        } else {
            // Ensure status and registration_date exist in user table if it already exists
            String[] userColumns = {"status", "registration_date"};
            String[] userDefinitions = {
                "ADD `status` VARCHAR(20) DEFAULT 'Actif'",
                "ADD `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            };

            for (int i = 0; i < userColumns.length; i++) {
                try {
                    ResultSet rs = md.getColumns(null, null, "user", userColumns[i]);
                    if (!rs.next()) {
                        System.out.println("--- Adding missing column '" + userColumns[i] + "' to 'user' table ---");
                        cnx.createStatement().execute("ALTER TABLE `user` " + userDefinitions[i]);
                    } else if (userColumns[i].equals("registration_date")) {
                        String def = rs.getString("COLUMN_DEF");
                        if (def == null || def.isEmpty()) {
                            System.out.println("--- Fixing missing default for 'registration_date' in 'user' table ---");
                            cnx.createStatement().execute("ALTER TABLE `user` MODIFY `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                        }
                    }
                } catch (SQLException e) {
                    System.out.println("Column '" + userColumns[i] + "' check/migration failed in user: " + e.getMessage());
                }
            }
        }

        // Check if user with ID 1 exists to prevent FK failures
        String checkUser = "SELECT count(*) FROM `user` WHERE `id` = 1";
        ResultSet rs = cnx.createStatement().executeQuery(checkUser);
        if (rs.next() && rs.getInt(1) == 0) {
            System.out.println("--- Creating fallback test user (ID=1) ---");
            String insertUser = "INSERT INTO `user` (`id`, `email`, `password`, `role`, `nom`, `prenom`, `status`) " +
                    "VALUES (1, 'test@example.com', 'test123', 'USER', 'Test', 'User', 'Actif')";
            try {
                cnx.createStatement().execute(insertUser);
            } catch (SQLException e) {
                System.out.println("Failed to create fallback user: " + e.getMessage());
            }
        }
    }
}
