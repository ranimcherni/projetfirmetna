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

    public void deleteAll() {
        String req = "DELETE FROM `produit` ";
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(req);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de tous les produits: " + e.getMessage(), e);
        }
    }

    private void seedDefaultProductsIfEmpty() {
        deleteAll(); 

        if (countAll() > 0) {
            return;
        }

        // --- ANIMAUX ---
        add(new Produit("Miel de Fleurs", "Miel artisanal 100% naturel récolté localement.", 14.5, "animale",
                "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400", "pot", 20, true, "Premium"));
        
        add(new Produit("Oeufs de Ferme", "Oeufs de poules élevées en plein air.", 5.8, "animale",
                "https://images.unsplash.com/photo-1582722872445-44ad5c78a1dd?w=400", "douzaine", 40, true, "Bio"));
        
        add(new Produit("Lait Frais Cru", "Lait de vache entier riche et crémeux.", 3.2, "animale",
                "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=400", "litre", 45, false, "Frais"));
        
        add(new Produit("Fromage de Chèvre", "Fromage de chèvre affiné artisanalement.", 18.0, "animale",
                "https://images.unsplash.com/photo-1486297678162-ad2a19b05840?w=400", "pièce", 15, false, "Artisanal"));

        add(new Produit("Poulet Fermier", "Poulet élevé en liberté, chair ferme et goûteuse.", 22.0, "animale",
                "https://images.unsplash.com/photo-1588615413541-74ecaf11d9a7?w=400", "pièce", 10, true, "Label Rouge"));

        add(new Produit("Beurre de Baratte", "Beurre artisanal salé à la fleur de sel.", 4.5, "animale",
                "https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=400", "250g", 25, false, "Frais"));

        // --- VÉGÉTAUX ---
        add(new Produit("Pommes Gala", "Pommes rouges sucrées et croquantes.", 4.5, "vegetale",
                "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400", "kg", 75, true, "Saison"));
        
        add(new Produit("Huile d'Olive", "Huile d'olive extra vierge, première pression.", 21.0, "vegetale",
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400", "litre", 30, true, "Premium"));

        add(new Produit("Fraises Gariguette", "Fraises parfumées cueillies le matin.", 7.5, "vegetale",
                "https://images.unsplash.com/photo-1518635017498-87f514b751ba?w=400", "barquette", 20, true, "Bio"));

        add(new Produit("Panier de Saison", "Mélange de légumes frais du potager.", 12.0, "vegetale",
                "https://images.unsplash.com/photo-1566385102321-1975771393c2?w=400", "panier", 15, true, "Mixte"));

        add(new Produit("Farine Complète", "Farine de blé moulue à la pierre.", 3.5, "vegetale",
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400", "kg", 40, true, "Artisanal"));

        add(new Produit("Amandes Grillées", "Amandes locales délicatement toastées.", 9.0, "vegetale",
                "https://images.unsplash.com/photo-1508061253366-f7da158b6d46?w=400", "sac", 50, false, "Croquant"));
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
