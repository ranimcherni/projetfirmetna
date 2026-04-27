package services;

import interfaces.IService;
import models.User;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserService implements IService<User> {

    private final Connection cnx;

    public UserService() {
        this(MyDataBase.getInstance().getCnx(), true);
    }

    public UserService(Connection cnx) {
        this(cnx, false);
    }

    private UserService(Connection cnx, boolean runMigration) {
        this.cnx = cnx;
        ensureConnection();
        if (runMigration) {
            migrate();
        }
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException(
                    "Database connection is unavailable. Verify MySQL is running and the firmetna_new_db database is reachable.");
        }
    }

    private void migrate() {
        try {
            DatabaseMetaData md = cnx.getMetaData();

            if (!hasColumn(md, "user", "status")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `status` VARCHAR(20) DEFAULT 'Actif'");
                System.out.println("--- Migration: 'status' column added ---");
            }
            if (!hasColumn(md, "user", "registration_date")) {
                cnx.createStatement()
                        .execute("ALTER TABLE `user` ADD `registration_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                System.out.println("--- Migration: 'registration_date' column added ---");
            }
            if (!hasColumn(md, "user", "date_inscription")) {
                cnx.createStatement()
                        .execute("ALTER TABLE `user` ADD `date_inscription` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
                System.out.println("--- Migration: 'date_inscription' column added ---");
            } else {
                cnx.createStatement()
                        .execute("ALTER TABLE `user` MODIFY `date_inscription` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
                System.out.println("--- Migration: 'date_inscription' column normalized ---");
            }
            if (!hasColumn(md, "user", "statut")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `statut` VARCHAR(20) DEFAULT 'Actif'");
                System.out.println("--- Migration: 'statut' column added for legacy compatibility ---");
            } else {
                cnx.createStatement()
                        .execute("ALTER TABLE `user` MODIFY `statut` VARCHAR(20) NOT NULL DEFAULT 'Actif'");
                System.out.println("--- Migration: 'statut' column normalized ---");
            }
            if (!hasColumn(md, "user", "reset_code")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `reset_code` VARCHAR(10) DEFAULT NULL");
                System.out.println("--- Migration: 'reset_code' column added ---");
            }
            if (!hasColumn(md, "user", "reset_expiry")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `reset_expiry` TIMESTAMP NULL DEFAULT NULL");
                System.out.println("--- Migration: 'reset_expiry' column added ---");
            }
            if (!hasColumn(md, "user", "mfa_secret")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `mfa_secret` VARCHAR(255) DEFAULT NULL");
                System.out.println("--- Migration: 'mfa_secret' column added ---");
            }
            if (!hasColumn(md, "user", "mfa_enabled")) {
                cnx.createStatement().execute("ALTER TABLE `user` ADD `mfa_enabled` TINYINT(1) DEFAULT 0");
                System.out.println("--- Migration: 'mfa_enabled' column added ---");
            }
        } catch (SQLException e) {
            System.err.println("Migration error: " + e.getMessage());
        }
    }

    @Override
    public void add(User user) {
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt());
        Timestamp registrationTimestamp = user.getRegistrationDate() != null
                ? user.getRegistrationDate()
                : new Timestamp(System.currentTimeMillis());

        try {
            List<ColumnInfo> columns = getInsertableColumns();
            List<String> insertColumns = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (ColumnInfo column : columns) {
                Object value = resolveInsertValue(column, user, hashedPassword, registrationTimestamp);
                if (value != SkipValue.INSTANCE) {
                    insertColumns.add("`" + column.name + "`");
                    values.add(value);
                }
            }

            if (insertColumns.isEmpty()) {
                throw new SQLException("No writable columns found for table `user`.");
            }

            String placeholders = String.join(", ", java.util.Collections.nCopies(values.size(), "?"));
            String req = "INSERT INTO `user` (" + String.join(", ", insertColumns) + ") VALUES (" + placeholders + ")";

            try (PreparedStatement pstm = cnx.prepareStatement(req)) {
                for (int i = 0; i < values.size(); i++) {
                    pstm.setObject(i + 1, values.get(i));
                }
                pstm.executeUpdate();
            }

            System.out.println("User ajoute avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(User user) {
        String passwordPart = (user.getPassword() != null && !user.getPassword().isEmpty()) ? ", `password`=?" : "";
        String req = "UPDATE `user` SET `email`=?, `role`=?, `nom`=?, `prenom`=?, `localisation`=?, `bio`=?, `specialite`=?, `telephone`=?, `status`=?, `mfa_secret`=?, `mfa_enabled`=?"
                + passwordPart + " WHERE `id`=?";
        String normalizedStatus = user.getStatus() != null ? user.getStatus() : "Actif";

        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, user.getEmail());
            pstm.setString(2, user.getRole());
            pstm.setString(3, user.getNom());
            pstm.setString(4, user.getPrenom());
            pstm.setString(5, user.getLocalisation());
            pstm.setString(6, user.getBio());
            pstm.setString(7, user.getSpecialite());
            pstm.setString(8, user.getTelephone());
            pstm.setString(9, normalizedStatus);
            pstm.setString(10, user.getMfaSecret());
            pstm.setBoolean(11, user.isMfaEnabled());

            if (!passwordPart.isEmpty()) {
                String p = user.getPassword();
                String hashedPassword = p.startsWith("$2a$") ? p : org.mindrot.jbcrypt.BCrypt.hashpw(p, org.mindrot.jbcrypt.BCrypt.gensalt());
                pstm.setString(12, hashedPassword);
                pstm.setInt(13, user.getId());
            } else {
                pstm.setInt(12, user.getId());
            }

            pstm.executeUpdate();
            syncLegacyStatus(user.getId(), normalizedStatus);
            System.out.println("User modifie avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(User user) {
        String req = "DELETE FROM `user` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, user.getId());
            pstm.executeUpdate();
            System.out.println("User supprime avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM `user` ORDER BY `registration_date` DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setLocalisation(rs.getString("localisation"));
                u.setBio(rs.getString("bio"));
                u.setSpecialite(rs.getString("specialite"));
                u.setTelephone(rs.getString("telephone"));
                u.setStatus(rs.getString("status"));
                u.setRegistrationDate(rs.getTimestamp("registration_date"));
                u.setMfaSecret(rs.getString("mfa_secret"));
                u.setMfaEnabled(rs.getBoolean("mfa_enabled"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return users;
    }

    public boolean existsByEmail(String email) {
        String req = "SELECT count(*) FROM `user` WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("existsByEmail error: " + e.getMessage());
        }
        return false;
    }

    public User getUserByEmail(String email) {
        String req = "SELECT * FROM `user` WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setMfaSecret(rs.getString("mfa_secret"));
                u.setMfaEnabled(rs.getBoolean("mfa_enabled"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("getUserByEmail error: " + e.getMessage());
        }
        return null;
    }

    public boolean isEmailTaken(String email, int currentUserId) {
        String req = "SELECT count(*) FROM `user` WHERE `email` = ? AND `id` != ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            pstm.setInt(2, currentUserId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("isEmailTaken error: " + e.getMessage());
        }
        return false;
    }

    public void setResetCode(String email, String code, Timestamp expiry) {
        String req = "UPDATE `user` SET `reset_code` = ?, `reset_expiry` = ? WHERE `email` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, code);
            pstm.setTimestamp(2, expiry);
            pstm.setString(3, email);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("setResetCode error: " + e.getMessage());
        }
    }

    public boolean validateResetCode(String email, String code) {
        String req = "SELECT `reset_expiry` FROM `user` WHERE `email` = ? AND `reset_code` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, email);
            pstm.setString(2, code);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Timestamp expiry = rs.getTimestamp("reset_expiry");
                if (expiry != null && expiry.getTime() > System.currentTimeMillis()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("validateResetCode error: " + e.getMessage());
        }
        return false;
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        String req = "UPDATE `user` SET `password` = ?, `reset_code` = NULL, `reset_expiry` = NULL WHERE `email` = ?";
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setString(1, hashedPassword);
            pstm.setString(2, email);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("updatePasswordByEmail error: " + e.getMessage());
        }
    }

    private void syncLegacyStatus(int userId, String status) {
        try {
            if (!hasColumn(cnx.getMetaData(), "user", "statut")) {
                return;
            }
            try (PreparedStatement stmt = cnx.prepareStatement("UPDATE `user` SET `statut`=? WHERE `id`=?")) {
                stmt.setString(1, status != null ? status : "Actif");
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Legacy status sync error: " + e.getMessage());
        }
    }

    private List<ColumnInfo> getInsertableColumns() throws SQLException {
        Map<String, ColumnInfo> columns = new LinkedHashMap<>();
        DatabaseMetaData md = cnx.getMetaData();
        try (ResultSet rs = md.getColumns(null, null, "user", null)) {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                if ("id".equalsIgnoreCase(name)) {
                    continue;
                }
                columns.putIfAbsent(name.toLowerCase(Locale.ROOT), new ColumnInfo(
                        name,
                        rs.getInt("DATA_TYPE"),
                        rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                        rs.getString("COLUMN_DEF")));
            }
        }
        return new ArrayList<>(columns.values());
    }

    private Object resolveInsertValue(ColumnInfo column, User user, String hashedPassword, Timestamp registrationTimestamp) {
        String name = column.name.toLowerCase(Locale.ROOT);
        switch (name) {
            case "email":
                return valueOrRequiredFallback(user.getEmail(), column);
            case "password":
                return valueOrRequiredFallback(hashedPassword, column);
            case "role":
            case "type":
                return valueOrRequiredFallback(user.getRole(), column);
            case "nom":
                return valueOrRequiredFallback(user.getNom(), column);
            case "prenom":
                return valueOrRequiredFallback(user.getPrenom(), column);
            case "localisation":
            case "location":
            case "adresse":
            case "address":
                return valueOrRequiredFallback(user.getLocalisation(), column);
            case "bio":
                return valueOrRequiredFallback(user.getBio(), column);
            case "specialite":
            case "speciality":
                return valueOrRequiredFallback(user.getSpecialite(), column);
            case "telephone":
            case "phone":
            case "num_tel":
                return valueOrRequiredFallback(user.getTelephone(), column);
            case "image":
            case "avatar":
            case "photo":
                return valueOrRequiredFallback(user.getImage(), column);
            case "status":
            case "statut":
                return valueOrRequiredFallback(user.getStatus() != null ? user.getStatus() : "Actif", column);
            case "registration_date":
            case "date_inscription":
            case "created_at":
                return registrationTimestamp;
            case "reset_code":
                return valueOrRequiredFallback(user.getResetCode(), column);
            case "reset_expiry":
                return valueOrRequiredFallback(user.getResetExpiry(), column);
            case "mfa_secret":
                return valueOrRequiredFallback(user.getMfaSecret(), column);
            case "mfa_enabled":
                return user.isMfaEnabled();
            default:
                return defaultOrSkip(column);
        }
    }

    private Object valueOrRequiredFallback(Object value, ColumnInfo column) {
        return value != null ? value : defaultOrSkip(column);
    }

    private Object defaultOrSkip(ColumnInfo column) {
        if (column.hasDefault || column.nullable) {
            return SkipValue.INSTANCE;
        }

        switch (column.sqlType) {
            case Types.BOOLEAN:
            case Types.BIT:
                return false;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return 0;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.DATE:
            case Types.TIME:
                return new Timestamp(System.currentTimeMillis());
            default:
                return "";
        }
    }

    private boolean hasColumn(DatabaseMetaData md, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = md.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static final class ColumnInfo {
        private final String name;
        private final int sqlType;
        private final boolean nullable;
        private final boolean hasDefault;

        private ColumnInfo(String name, int sqlType, boolean nullable, String defaultValue) {
            this.name = name;
            this.sqlType = sqlType;
            this.nullable = nullable;
            this.hasDefault = defaultValue != null;
        }
    }

    private enum SkipValue {
        INSTANCE
    }
}
