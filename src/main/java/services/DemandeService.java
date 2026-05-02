package services;

import interfaces.IService;
import models.Demande;
import models.Offre;
import models.User;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeService implements IService<Demande> {
    private Connection cnx;
    private final OffreService offreService;

    public DemandeService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        this.offreService = new OffreService(this.cnx);
        if (this.cnx != null) {
            migrate();
        } else {
            System.err.println("DemandeService: Connection is null, skipping migration.");
        }
    }

    public DemandeService(Connection cnx) {
        this.cnx = cnx;
        this.offreService = new OffreService(cnx);
    }

    private void migrate() {
        if (cnx == null) return;
        try {
            String sql = "CREATE TABLE IF NOT EXISTS `demande` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "`statut` VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE'," +
                    "`offre_id` INT NOT NULL," +
                    "`demandeur_id` INT NOT NULL," +
                    "`quantite_demandee` INT NOT NULL DEFAULT 1," +
                    "`disponible` TINYINT(1) NOT NULL DEFAULT 1," +
                    "`date_recuperation` TIMESTAMP NULL DEFAULT NULL," +
                    "PRIMARY KEY (`id`)," +
                    "KEY `idx_demande_offre` (`offre_id`)," +
                    "KEY `idx_demande_demandeur` (`demandeur_id`)," +
                    "CONSTRAINT `fk_demande_offre` FOREIGN KEY (`offre_id`) REFERENCES `offre` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                    "CONSTRAINT `fk_demande_user` FOREIGN KEY (`demandeur_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            cnx.createStatement().execute(sql);

            try {
                cnx.createStatement().execute("ALTER TABLE `demande` ADD COLUMN `date_recuperation` TIMESTAMP NULL DEFAULT NULL");
            } catch (SQLException e) {
                // Ignore if the column already exists.
            }
        } catch (SQLException e) {
            System.err.println("Migration demande error: " + e.getMessage());
        }
    }

    @Override
    public void add(Demande demande) {
        addWithValidation(demande);
    }

    public boolean addWithValidation(Demande demande) {
        if (cnx == null || demande == null || demande.getOffre() == null || demande.getDemandeur() == null) return false;
        if (!isDemandeValide(demande)) {
            System.err.println("La quantite demandee depasse la quantite disponible dans l'offre.");
            return false;
        }

        String req = "INSERT INTO `demande` (`created_at`, `statut`, `offre_id`, `demandeur_id`, `quantite_demandee`, `disponible`, `date_recuperation`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setTimestamp(1, demande.getCreatedAt() != null ? demande.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            pstm.setString(2, demande.getStatut() != null ? demande.getStatut() : "EN_ATTENTE");
            pstm.setInt(3, demande.getOffre().getId());
            pstm.setInt(4, demande.getDemandeur().getId());
            pstm.setInt(5, demande.getQuantiteDemandee());
            pstm.setBoolean(6, demande.isDisponible());
            if (demande.getDateRecuperation() != null) {
                pstm.setTimestamp(7, Timestamp.valueOf(demande.getDateRecuperation()));
            } else {
                pstm.setTimestamp(7, null);
            }
            pstm.executeUpdate();
            System.out.println("Demande ajoutee avec succes !");
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public void update(Demande demande) {
        if (cnx == null || demande == null || demande.getOffre() == null || demande.getDemandeur() == null) return;
        if (!isDemandeValide(demande)) {
            System.err.println("Mise a jour refusee: quantite demandee > quantite de l'offre.");
            return;
        }

        String req = "UPDATE `demande` SET `created_at`=?, `statut`=?, `offre_id`=?, `demandeur_id`=?, `quantite_demandee`=?, `disponible`=?, `date_recuperation`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setTimestamp(1, demande.getCreatedAt() != null ? demande.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
            pstm.setString(2, demande.getStatut());
            pstm.setInt(3, demande.getOffre().getId());
            pstm.setInt(4, demande.getDemandeur().getId());
            pstm.setInt(5, demande.getQuantiteDemandee());
            pstm.setBoolean(6, demande.isDisponible());
            if (demande.getDateRecuperation() != null) {
                pstm.setTimestamp(7, Timestamp.valueOf(demande.getDateRecuperation()));
            } else {
                pstm.setTimestamp(7, null);
            }
            pstm.setInt(8, demande.getId());
            pstm.executeUpdate();
            System.out.println("Demande modifiee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Demande demande) {
        if (cnx == null || demande == null) return;
        String req = "DELETE FROM `demande` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, demande.getId());
            pstm.executeUpdate();
            System.out.println("Demande supprimee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Demande> getAll() {
        List<Demande> demandes = new ArrayList<>();
        if (cnx == null) return demandes;
        String req = "SELECT d.*, " +
                "o.telephone AS offre_telephone, o.categorie AS offre_categorie, o.description AS offre_description, o.photo AS offre_photo, o.quantite AS offre_quantite, " +
                "u.email AS user_email, u.nom AS user_nom, u.prenom AS user_prenom, u.telephone AS user_telephone " +
                "FROM `demande` d " +
                "JOIN `offre` o ON o.id = d.offre_id " +
                "JOIN `user` u ON u.id = d.demandeur_id " +
                "ORDER BY d.id DESC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                demandes.add(mapRowWithJoin(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return demandes;
    }

    public Demande getById(int id) {
        if (cnx == null) return null;
        String req = "SELECT d.*, " +
                "o.telephone AS offre_telephone, o.categorie AS offre_categorie, o.description AS offre_description, o.photo AS offre_photo, o.quantite AS offre_quantite, " +
                "u.email AS user_email, u.nom AS user_nom, u.prenom AS user_prenom, u.telephone AS user_telephone " +
                "FROM `demande` d " +
                "JOIN `offre` o ON o.id = d.offre_id " +
                "JOIN `user` u ON u.id = d.demandeur_id " +
                "WHERE d.id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return mapRowWithJoin(rs);
            }
        } catch (SQLException e) {
            System.err.println("getById demande error: " + e.getMessage());
        }
        return null;
    }

    public boolean isQuantiteDemandeeValide(int offreId, int quantiteDemandee) {
        if (quantiteDemandee <= 0) return false;
        Offre offre = offreService.getById(offreId);
        return offre != null && quantiteDemandee <= offre.getQuantite();
    }

    public boolean isDateRecuperationValide(LocalDateTime dateRecuperation) {
        if (dateRecuperation == null) return true;
        return !dateRecuperation.isBefore(LocalDateTime.now());
    }

    public boolean isDemandeValide(Demande demande) {
        if (demande == null || demande.getOffre() == null || demande.getDemandeur() == null) return false;
        if (demande.getOffre().getId() <= 0 || demande.getDemandeur().getId() <= 0) return false;
        if (!isQuantiteDemandeeValide(demande.getOffre().getId(), demande.getQuantiteDemandee())) return false;
        return isDateRecuperationValide(demande.getDateRecuperation());
    }

    public boolean updateDateRecuperation(int demandeId, LocalDateTime dateRecuperation) {
        if (cnx == null || demandeId <= 0 || !isDateRecuperationValide(dateRecuperation)) return false;
        Demande demande = getById(demandeId);
        if (demande == null || !isDemandeValide(demande)) return false;

        String req = "UPDATE `demande` SET `date_recuperation`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(req);
            pstm.setTimestamp(1, dateRecuperation != null ? Timestamp.valueOf(dateRecuperation) : null);
            pstm.setInt(2, demandeId);
            return pstm.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateDateRecuperation error: " + e.getMessage());
            return false;
        }
    }

    public List<Demande> getAllRecuperations() {
        List<Demande> demandes = new ArrayList<>();
        if (cnx == null) return demandes;
        String req = "SELECT d.*, " +
                "o.telephone AS offre_telephone, o.categorie AS offre_categorie, o.description AS offre_description, o.photo AS offre_photo, o.quantite AS offre_quantite, " +
                "u.email AS user_email, u.nom AS user_nom, u.prenom AS user_prenom, u.telephone AS user_telephone " +
                "FROM `demande` d " +
                "JOIN `offre` o ON o.id = d.offre_id " +
                "JOIN `user` u ON u.id = d.demandeur_id " +
                "WHERE d.date_recuperation IS NOT NULL " +
                "ORDER BY d.date_recuperation ASC";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                demandes.add(mapRowWithJoin(rs));
            }
        } catch (SQLException e) {
            System.err.println("getAllRecuperations error: " + e.getMessage());
        }
        return demandes;
    }

    private Demande mapRowWithJoin(ResultSet rs) throws SQLException {
        Offre offre = new Offre();
        offre.setId(rs.getInt("offre_id"));
        offre.setTelephone(rs.getString("offre_telephone"));
        offre.setCategorie(rs.getString("offre_categorie"));
        offre.setDescription(rs.getString("offre_description"));
        offre.setPhoto(rs.getString("offre_photo"));
        offre.setQuantite(rs.getInt("offre_quantite"));

        User demandeur = new User();
        demandeur.setId(rs.getInt("demandeur_id"));
        demandeur.setEmail(rs.getString("user_email"));
        demandeur.setNom(rs.getString("user_nom"));
        demandeur.setPrenom(rs.getString("user_prenom"));
        demandeur.setTelephone(rs.getString("user_telephone"));

        Demande demande = new Demande();
        demande.setId(rs.getInt("id"));
        demande.setCreatedAt(rs.getTimestamp("created_at"));
        demande.setStatut(rs.getString("statut"));
        demande.setOffre(offre);
        demande.setDemandeur(demandeur);
        demande.setQuantiteDemandee(rs.getInt("quantite_demandee"));
        demande.setDisponible(rs.getBoolean("disponible"));
        Timestamp ts = rs.getTimestamp("date_recuperation");
        demande.setDateRecuperation(ts != null ? ts.toLocalDateTime() : null);
        return demande;
    }
}
