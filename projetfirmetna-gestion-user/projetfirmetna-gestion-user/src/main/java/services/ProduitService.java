package services;

import interfaces.IService;
import models.Produit;
import utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProduitService implements IService<Produit> {

    private final Connection cnx;

    public ProduitService() {
        this.cnx = MyDataBase.getInstance().getCnx();
        ensureConnection();
        seedDefaultProductsIfEmpty();
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException(
                    "Database connection is unavailable. Verify MySQL is running and firmetna_new_db is reachable.");
        }
    }

    @Override
    public void add(Produit produit) {
        String req = """
                INSERT INTO `produit`
                (`nom`, `description`, `prix`, `type`, `image_url`, `unite`, `stock`, `is_bio`, `badge`, `created_at`, `updated_at`)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            pstm.setString(1, produit.getNom());
            pstm.setString(2, produit.getDescription());
            pstm.setDouble(3, produit.getPrix());
            pstm.setString(4, normalizeType(produit.getType()));
            pstm.setString(5, produit.getImageUrl());
            pstm.setString(6, produit.getUnite());
            pstm.setInt(7, produit.getStock());
            pstm.setBoolean(8, produit.isBio());
            pstm.setString(9, produit.getBadge());
            pstm.setTimestamp(10, produit.getCreatedAt() != null ? produit.getCreatedAt() : now);
            pstm.setTimestamp(11, produit.getUpdatedAt());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du produit: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Produit produit) {
        String req = """
                UPDATE `produit`
                SET `nom`=?, `description`=?, `prix`=?, `type`=?, `image_url`=?, `unite`=?, `stock`=?, `is_bio`=?, `badge`=?, `updated_at`=?
                WHERE `id`=?
                """;

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setString(1, produit.getNom());
            pstm.setString(2, produit.getDescription());
            pstm.setDouble(3, produit.getPrix());
            pstm.setString(4, normalizeType(produit.getType()));
            pstm.setString(5, produit.getImageUrl());
            pstm.setString(6, produit.getUnite());
            pstm.setInt(7, produit.getStock());
            pstm.setBoolean(8, produit.isBio());
            pstm.setString(9, produit.getBadge());
            pstm.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            pstm.setInt(11, produit.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du produit: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Produit produit) {
        String req = "DELETE FROM `produit` WHERE `id`=?";

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, produit.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du produit: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String req = "SELECT * FROM `produit` ORDER BY `created_at` DESC, `id` DESC";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                produits.add(mapProduit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation des produits: " + e.getMessage(), e);
        }

        return produits;
    }

    public List<Produit> getByType(String type) {
        if (type == null || type.isBlank() || "tous".equalsIgnoreCase(type)) {
            return getAll();
        }

        List<Produit> produits = new ArrayList<>();
        String req = "SELECT * FROM `produit` WHERE LOWER(`type`) = ? ORDER BY `created_at` DESC, `id` DESC";

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setString(1, normalizeType(type));
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapProduit(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du filtrage des produits: " + e.getMessage(), e);
        }

        return produits;
    }

    public Produit getById(int id) {
        String req = "SELECT * FROM `produit` WHERE `id`=?";

        try (PreparedStatement pstm = cnx.prepareStatement(req)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapProduit(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recuperation du produit: " + e.getMessage(), e);
        }

        return null;
    }

    public int countAll() {
        String req = "SELECT COUNT(*) FROM `produit`";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des produits: " + e.getMessage(), e);
        }
        return 0;
    }

    private Produit mapProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setNom(rs.getString("nom"));
        produit.setDescription(rs.getString("description"));
        produit.setPrix(rs.getDouble("prix"));
        produit.setType(rs.getString("type"));
        produit.setImageUrl(rs.getString("image_url"));
        produit.setUnite(rs.getString("unite"));
        produit.setStock(rs.getInt("stock"));
        produit.setBio(rs.getBoolean("is_bio"));
        produit.setBadge(rs.getString("badge"));
        produit.setCreatedAt(rs.getTimestamp("created_at"));
        produit.setUpdatedAt(rs.getTimestamp("updated_at"));
        return produit;
    }

    private void seedDefaultProductsIfEmpty() {
        if (countAll() > 0) {
            return;
        }

        add(new Produit("Tomates Bio", "Tomates fraiches cultivees biologiquement.", 3.5, "vegetale",
                "/esprit/tn/images/home3.png", "kilo", 50, true, "Bio"));
        add(new Produit("Carottes Fraiches", "Carottes croquantes de saison.", 2.0, "vegetale",
                "/esprit/tn/images/home.png", "kilo", 90, false, "Frais"));
        add(new Produit("Pommes Fermieres", "Pommes locales selectionnees a la main.", 4.5, "vegetale",
                "/esprit/tn/images/front2.png", "kilo", 75, true, "Fermier"));
        add(new Produit("Lait Frais", "Lait fermier du jour.", 3.8, "animale",
                "/esprit/tn/images/farmer_header.png", "litre", 45, false, "Nouveau"));
        add(new Produit("Oeufs Fermiers", "Oeufs frais de poules elevees a la ferme.", 5.0, "animale",
                "/esprit/tn/images/mixed_farm_bg.png", "boite", 40, false, "Frais"));
        add(new Produit("Miel Local", "Miel artisanal naturel produit localement.", 12.0, "animale",
                "/esprit/tn/images/image2.png", "pot", 20, true, "Premium"));
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "vegetale";
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("anim")) {
            return "animale";
        }
        return "vegetale";
    }
}
