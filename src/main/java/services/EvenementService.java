package services;

import interfaces.IService;
import models.Evenement;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {

    private Connection cnx;
    private final ParticipationService participationService;

    public EvenementService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.participationService = new ParticipationService(this.cnx);
        if (this.cnx != null) {
            new LieuService();
            migrate();
        } else {
            System.err.println("EvenementService: Connection is null, skipping migration.");
        }
    }

    public EvenementService(Connection cnx) {
        this.cnx = cnx;
        this.participationService = new ParticipationService(this.cnx);
    }

    private void migrate() {
        if (cnx == null) return;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `evenement` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`lieu_id` INT NOT NULL," +
                    "`nom` VARCHAR(200) NOT NULL," +
                    "`description` TEXT," +
                    "`date_evenement` DATE NOT NULL," +
                    "`organisateur` VARCHAR(200) DEFAULT NULL," +
                    "`image` VARCHAR(500) DEFAULT NULL," +
                    "PRIMARY KEY (`id`)," +
                    "KEY `idx_evenement_lieu` (`lieu_id`)," +
                    "CONSTRAINT `fk_evenement_lieu` FOREIGN KEY (`lieu_id`) REFERENCES `lieu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            cnx.createStatement().execute(sql);

            // Ensure 'image' column exists for users who already had the table
            try {
                cnx.createStatement().execute("ALTER TABLE `evenement` ADD COLUMN `image` VARCHAR(500) DEFAULT NULL");
                System.out.println("Column 'image' added to 'evenement' table.");
            } catch (SQLException e) {
                // Ignore if column already exists (Error 1060)
            }

            // Ensure 'ON DELETE CASCADE' is active for existing users
            try {
                cnx.createStatement().execute("ALTER TABLE `evenement` DROP FOREIGN KEY `fk_evenement_lieu`");
                cnx.createStatement().execute("ALTER TABLE `evenement` ADD CONSTRAINT `fk_evenement_lieu` " +
                        "FOREIGN KEY (`lieu_id`) REFERENCES `lieu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE");
                System.out.println("Constraint 'fk_evenement_lieu' updated to CASCADE.");
            } catch (SQLException e) {
                // Ignore if it fails (e.g. if it already has CASCADE or another constraint)
            }
        } catch (SQLException e) {
            System.err.println("Migration evenement error: " + e.getMessage());
        }
    }

    @Override
    public void add(Evenement e) {
        if (cnx == null) return;
        String req = "INSERT INTO `evenement` (`lieu_id`, `nom`, `description`, `date_evenement`, `organisateur`, `image`) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, e.getLieuId());
            pstm.setString(2, e.getNom());
            pstm.setString(3, e.getDescription());
            pstm.setDate(4, e.getDateEvenement());
            pstm.setString(5, e.getOrganisateur());
            pstm.setString(6, e.getImage());
            pstm.executeUpdate();
            System.out.println("Événement ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void update(Evenement e) {
        if (cnx == null) return;
        String req = "UPDATE `evenement` SET `lieu_id`=?, `nom`=?, `description`=?, `date_evenement`=?, `organisateur`=?, `image`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, e.getLieuId());
            pstm.setString(2, e.getNom());
            pstm.setString(3, e.getDescription());
            pstm.setDate(4, e.getDateEvenement());
            pstm.setString(5, e.getOrganisateur());
            pstm.setString(6, e.getImage());
            pstm.setInt(7, e.getId());
            pstm.executeUpdate();
            System.out.println("Événement modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void delete(Evenement e) {
        if (cnx == null) return;
        String req = "DELETE FROM `evenement` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, e.getId());
            pstm.executeUpdate();
            System.out.println("Événement supprimé avec succès !");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        List<Evenement> list = new ArrayList<>();
        if (cnx == null) return list;
        String req = "SELECT e.`id`, e.`lieu_id`, e.`nom`, e.`description`, e.`date_evenement`, e.`organisateur`, e.`image`, " +
                "l.`ville` AS `lieu_ville`, l.`adresse` AS `lieu_adresse`, l.`capacite` AS `lieu_capacite` " +
                "FROM `evenement` e " +
                "JOIN `lieu` l ON l.`id` = e.`lieu_id` " +
                "ORDER BY e.`date_evenement` DESC, e.`id` DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(mapRowWithJoin(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    public Evenement getById(int id) {
        if (cnx == null) return null;
        String req = "SELECT e.`id`, e.`lieu_id`, e.`nom`, e.`description`, e.`date_evenement`, e.`organisateur`, e.`image`, " +
                "l.`ville` AS `lieu_ville`, l.`adresse` AS `lieu_adresse`, l.`capacite` AS `lieu_capacite` " +
                "FROM `evenement` e " +
                "JOIN `lieu` l ON l.`id` = e.`lieu_id` " +
                "WHERE e.`id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapRowWithJoin(rs);
            }
        } catch (SQLException ex) {
            System.err.println("getById evenement error: " + ex.getMessage());
        }
        return null;
    }

    private Evenement mapRowWithJoin(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setId(rs.getInt("id"));
        e.setLieuId(rs.getInt("lieu_id"));
        e.setNom(rs.getString("nom"));
        e.setDescription(rs.getString("description"));
        e.setDateEvenement(rs.getDate("date_evenement"));
        e.setOrganisateur(rs.getString("organisateur"));
        e.setImage(rs.getString("image"));
        e.setLieuVille(rs.getString("lieu_ville"));
        e.setLieuAdresse(rs.getString("lieu_adresse"));
        e.setLieuCapacite(rs.getInt("lieu_capacite"));
        return e;
    }

    // Service métier: réservation avec contrôle de capacité.
    public ReservationStatus reserverPlace(int evenementId, int userId) {
        if (participationService.hasParticipation(evenementId, userId)) {
            return ReservationStatus.ALREADY_PARTICIPATING;
        }
        if (isEvenementComplet(evenementId)) {
            return ReservationStatus.FULL;
        }
        if (participationService.addParticipation(evenementId, userId)) {
            return ReservationStatus.SUCCESS;
        }

        // Si l'insert a échoué à cause d'une contrainte d'unicité/concurrence, on renvoie un statut métier clair.
        if (participationService.hasParticipation(evenementId, userId)) {
            return ReservationStatus.ALREADY_PARTICIPATING;
        }
        if (isEvenementComplet(evenementId)) {
            return ReservationStatus.FULL;
        }
        return ReservationStatus.ERROR;
    }

    public int countParticipants(int evenementId) {
        return participationService.countParticipantsByEvenement(evenementId);
    }

    public boolean hasParticipation(int evenementId, int userId) {
        return participationService.hasParticipation(evenementId, userId);
    }

    public boolean isEvenementComplet(int evenementId) {
        Evenement ev = getById(evenementId);
        if (ev == null) return true;
        return countParticipants(evenementId) >= ev.getLieuCapacite();
    }

    public enum ReservationStatus {
        SUCCESS,
        FULL,
        ALREADY_PARTICIPATING,
        ERROR
    }
}
