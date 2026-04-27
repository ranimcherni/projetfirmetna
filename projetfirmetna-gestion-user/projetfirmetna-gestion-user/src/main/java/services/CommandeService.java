package services;

import interfaces.IService;
import models.Commande;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommandeService implements IService<Commande> {

    private final Connection cnx;

    public CommandeService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        ensureConnection();
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException(
                    "Database connection is unavailable. Verify MySQL is running and firmetna_new_db is reachable.");
        }
    }

    @Override
    public void add(Commande commande) {
        String req = """
                INSERT INTO `commande`
                (`date_commande`, `statut`, `adresse_livraison`, `total`, `commentaire`, `client_id`, `email`, `client`, `date`)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Date today = new Date(System.currentTimeMillis());
            pstm.setTimestamp(1, commande.getDateCommande() != null ? commande.getDateCommande() : now);
            pstm.setString(2, commande.getStatut());
            pstm.setString(3, commande.getAdresseLivraison());
            pstm.setBigDecimal(4, commande.getTotal());
            pstm.setString(5, commande.getCommentaire());
            pstm.setInt(6, commande.getClientId());
            pstm.setString(7, commande.getEmail());
            pstm.setString(8, commande.getClient());
            pstm.setDate(9, commande.getDate() != null ? commande.getDate() : today);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la commande: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Commande commande) {
        String req = """
                UPDATE `commande`
                SET `date_commande`=?, `statut`=?, `adresse_livraison`=?, `total`=?, `commentaire`=?, `client_id`=?, `email`=?, `client`=?, `date`=?
                WHERE `id`=?
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setTimestamp(1, commande.getDateCommande());
            pstm.setString(2, commande.getStatut());
            pstm.setString(3, commande.getAdresseLivraison());
            pstm.setBigDecimal(4, commande.getTotal());
            pstm.setString(5, commande.getCommentaire());
            pstm.setInt(6, commande.getClientId());
            pstm.setString(7, commande.getEmail());
            pstm.setString(8, commande.getClient());
            pstm.setDate(9, commande.getDate());
            pstm.setInt(10, commande.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de la commande: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Commande commande) {
        String req = "DELETE FROM `commande` WHERE `id`=?";

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, commande.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commande> getAll() {
        List<Commande> commandes = new ArrayList<>();
        String req = "SELECT * FROM `commande` ORDER BY `date_commande` DESC, `id` DESC";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                commandes.add(mapCommande(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des commandes: " + e.getMessage(), e);
        }

        return commandes;
    }

    public Commande getById(int id) {
        String req = "SELECT * FROM `commande` WHERE `id`=?";

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapCommande(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation de la commande: " + e.getMessage(), e);
        }

        return null;
    }

    private Commande mapCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));
        commande.setDateCommande(rs.getTimestamp("date_commande"));
        commande.setStatut(rs.getString("statut"));
        commande.setAdresseLivraison(rs.getString("adresse_livraison"));
        commande.setTotal(rs.getBigDecimal("total"));
        commande.setCommentaire(rs.getString("commentaire"));
        commande.setClientId(rs.getInt("client_id"));
        commande.setEmail(rs.getString("email"));
        commande.setClient(rs.getString("client"));
        commande.setDate(rs.getDate("date"));
        return commande;
    }
}
