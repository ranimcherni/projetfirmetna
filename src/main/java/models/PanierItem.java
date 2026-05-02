package models;

import java.math.BigDecimal;

public class PanierItem {
    private final Produit produit;
    private int quantite;

    public PanierItem(Produit produit, int quantite) {
        this.produit = produit;
        this.quantite = quantite;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getSousTotal() {
        return BigDecimal.valueOf(produit.getPrix()).multiply(BigDecimal.valueOf(quantite));
    }
}
