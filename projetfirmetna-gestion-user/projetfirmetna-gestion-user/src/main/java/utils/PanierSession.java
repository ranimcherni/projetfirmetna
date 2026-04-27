package utils;

import models.PanierItem;
import models.Produit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PanierSession {
    private static PanierSession instance;
    private final List<PanierItem> items = new ArrayList<>();

    private PanierSession() {
    }

    public static synchronized PanierSession getInstance() {
        if (instance == null) {
            instance = new PanierSession();
        }
        return instance;
    }

    public List<PanierItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addProduit(Produit produit) {
        addProduit(produit, 1);
    }

    public void addProduit(Produit produit, int quantite) {
        if (quantite <= 0) return;
        for (PanierItem item : items) {
            if (item.getProduit().getId() == produit.getId()) {
                item.setQuantite(item.getQuantite() + quantite);
                return;
            }
        }
        items.add(new PanierItem(produit, quantite));
    }

    public void removeProduit(int produitId) {
        items.removeIf(item -> item.getProduit().getId() == produitId);
    }

    public void updateQuantite(int produitId, int quantite) {
        for (PanierItem item : items) {
            if (item.getProduit().getId() == produitId) {
                if (quantite <= 0) {
                    removeProduit(produitId);
                } else {
                    item.setQuantite(quantite);
                }
                return;
            }
        }
    }

    public int getNombreArticles() {
        return items.stream().mapToInt(PanierItem::getQuantite).sum();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(PanierItem::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}
