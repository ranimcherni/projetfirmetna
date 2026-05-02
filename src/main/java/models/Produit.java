package models;

import java.sql.Timestamp;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private String type;
    private String imageUrl;
    private String unite;
    private int stock;
    private boolean bio;
    private String badge;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Produit() {
    }

    public Produit(String nom, String description, double prix, String type, String imageUrl, String unite, int stock,
            boolean bio, String badge) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.type = type;
        this.imageUrl = imageUrl;
        this.unite = unite;
        this.stock = stock;
        this.bio = bio;
        this.badge = badge;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public boolean isBio() {
        return bio;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prix=" + prix +
                ", type='" + type + '\'' +
                ", stock=" + stock +
                '}';
    }
}
