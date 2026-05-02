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
            seedSampleData(cnx);
            System.out.println("--- Database Initialization/Repair Complete ---");
            initialized = true;
        } catch (SQLException e) {
            System.err.println("Database Initialization Error: " + e.getMessage());
        }
    }

    private static void seedSampleData(Connection cnx) throws SQLException {
        Statement stm = cnx.createStatement();
        
        // 1. Always Ensure 5 Sample Publications with Images (using REPLACE INTO)
        System.out.println("--- Syncing 5 Premium Sample Publications ---");
        String[] insertPubs = {
            "REPLACE INTO `publication` (id, titre, contenu, type, auteur_id, image_path) VALUES (1, 'Agriculture Bio : Les bases', 'L agriculture biologique est un système de production qui maintient la santé des sols, des écosystèmes et des personnes.', 'Discussion', 1, 'https://images.unsplash.com/photo-1594411130761-41270274f884')",
            "REPLACE INTO `publication` (id, titre, contenu, type, auteur_id, image_path) VALUES (2, 'Sustainable Farming 2024', 'Modern sustainable farming involves using technology to reduce water consumption and improve soil quality. This is vital for the future.', 'Discussion', 1, 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6')",
            "REPLACE INTO `publication` (id, titre, contenu, type, auteur_id, image_path) VALUES (3, 'Alerte Météo : Gelées tardives', 'Attention amis agriculteurs, une vague de froid est prévue pour demain matin. Protégez vos jeunes plants !', 'Annonce', 1, 'https://images.unsplash.com/photo-1500382017468-9049fed747ef')",
            "REPLACE INTO `publication` (id, titre, contenu, type, auteur_id, image_path) VALUES (4, 'Récolte des Légumes Frais', 'Nos premiers paniers de saison sont prêts ! Venez découvrir la fraîcheur de nos terres.', 'Discussion', 1, 'https://images.unsplash.com/photo-1566385270613-24006dad3d30')",
            "REPLACE INTO `publication` (id, titre, contenu, type, auteur_id, image_path) VALUES (5, 'Nos Moutons en Plein Air', 'L élevage responsable est au coeur de notre mission chez Firmetna.', 'Discussion', 1, 'https://images.unsplash.com/photo-1484557985045-dee256831382')"
        };
        for (String sql : insertPubs) {
            stm.execute(sql);
        }

        // 2. Insert Comments (Only if empty)
        ResultSet rs = stm.executeQuery("SELECT count(*) FROM `commentaire` ");
        if (rs.next() && rs.getInt(1) == 0) {
            String insertComm1 = "INSERT INTO `commentaire` (contenu, auteur_id, publication_id) VALUES " +
                "('Merci pour ces informations précieuses !', 1, 1)";
            String insertComm2 = "INSERT INTO `commentaire` (contenu, auteur_id, publication_id) VALUES " +
                "('Is it possible to automate these techniques?', 1, 2)";
            
            stm.execute(insertComm1);
            stm.execute(insertComm2);

            // 3. Insert Reactions
            String insertReact1 = "INSERT INTO `reaction` (user_id, target_type, target_id, type) VALUES (1, 'publication', 1, 'like')";
            String insertReact2 = "INSERT INTO `reaction` (user_id, target_type, target_id, type) VALUES (1, 'publication', 2, 'dislike')";
            
            stm.execute(insertReact1);
            stm.execute(insertReact2);
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

        // 4. Create Partner Table
        String createPartner = "CREATE TABLE IF NOT EXISTS `partner` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`name` VARCHAR(255) NOT NULL, " +
                "`type` VARCHAR(100) NOT NULL, " +
                "`email` VARCHAR(255) UNIQUE NOT NULL, " +
                "`phone` VARCHAR(50) UNIQUE NOT NULL, " +
                "`address` TEXT NOT NULL, " +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        stm.execute(createPartner);

        // 5. Create Contract Table
        String createContract = "CREATE TABLE IF NOT EXISTS `contract` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY, " +
                "`partner_id` INT NOT NULL, " +
                "`title` VARCHAR(255) NOT NULL, " +
                "`description` TEXT NOT NULL, " +
                "`start_date` DATE NOT NULL, " +
                "`end_date` DATE NOT NULL, " +
                "`value` DOUBLE NOT NULL, " +
                "`status` VARCHAR(50) DEFAULT 'En cours', " +
                "FOREIGN KEY (`partner_id`) REFERENCES `partner`(`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        stm.execute(createContract);
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
