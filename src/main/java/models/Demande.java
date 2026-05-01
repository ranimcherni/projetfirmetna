package models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Demande {
    private int id;
    private Timestamp createdAt;
    private String statut;
    private Offre offre;
    private User demandeur;
    private int quantiteDemandee;
    private boolean disponible;
    private LocalDateTime dateRecuperation;

    public Demande() {
    }

    public Demande(Timestamp createdAt, String statut, Offre offre, User demandeur, int quantiteDemandee, boolean disponible, LocalDateTime dateRecuperation) {
        this.createdAt = createdAt;
        this.statut = statut;
        this.offre = offre;
        this.demandeur = demandeur;
        this.quantiteDemandee = quantiteDemandee;
        this.disponible = disponible;
        this.dateRecuperation = dateRecuperation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public User getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(User demandeur) {
        this.demandeur = demandeur;
    }

    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }

    public void setQuantiteDemandee(int quantiteDemandee) {
        this.quantiteDemandee = quantiteDemandee;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getDateRecuperation() {
        return dateRecuperation;
    }

    public void setDateRecuperation(LocalDateTime dateRecuperation) {
        this.dateRecuperation = dateRecuperation;
    }
}
