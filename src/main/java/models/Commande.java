package models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Commande {
    private int id;
    private Timestamp dateCommande;
    private String statut;
    private String adresseLivraison;
    private BigDecimal total;
    private String commentaire;
    private int clientId;
    private String email;
    private String client;
    private Date date;

    public Commande() {
    }

    public Commande(Timestamp dateCommande, String statut, String adresseLivraison, BigDecimal total, String commentaire,
            int clientId, String email, String client, Date date) {
        this.dateCommande = dateCommande;
        this.statut = statut;
        this.adresseLivraison = adresseLivraison;
        this.total = total;
        this.commentaire = commentaire;
        this.clientId = clientId;
        this.email = email;
        this.client = client;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(Timestamp dateCommande) {
        this.dateCommande = dateCommande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", statut='" + statut + '\'' +
                ", total=" + total +
                ", clientId=" + clientId +
                '}';
    }
}
