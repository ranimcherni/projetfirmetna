package models;

import java.sql.Date;

public class Evenement {
    private int id;
    private int lieuId;
    private String nom;
    private String description;
    private Date dateEvenement;
    private String organisateur;
    private String image;

    /** Rempli par JOIN (affichage UI uniquement) */
    private String lieuVille;
    /** Rempli par JOIN (affichage UI uniquement) */
    private String lieuAdresse;
    /** Rempli par JOIN (affichage UI uniquement) */
    private int lieuCapacite;

    public Evenement() {
    }

    public Evenement(int lieuId, String nom, String description, Date dateEvenement, String organisateur, String image) {
        this.lieuId = lieuId;
        this.nom = nom;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.organisateur = organisateur;
        this.image = image;
    }

    public Evenement(int id, int lieuId, String nom, String description, Date dateEvenement, String organisateur, String image) {
        this.id = id;
        this.lieuId = lieuId;
        this.nom = nom;
        this.description = description;
        this.dateEvenement = dateEvenement;
        this.organisateur = organisateur;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLieuId() {
        return lieuId;
    }

    public void setLieuId(int lieuId) {
        this.lieuId = lieuId;
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

    public Date getDateEvenement() {
        return dateEvenement;
    }

    public void setDateEvenement(Date dateEvenement) {
        this.dateEvenement = dateEvenement;
    }

    public String getOrganisateur() {
        return organisateur;
    }

    public void setOrganisateur(String organisateur) {
        this.organisateur = organisateur;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLieuVille() {
        return lieuVille;
    }

    public void setLieuVille(String lieuVille) {
        this.lieuVille = lieuVille;
    }

    public String getLieuAdresse() {
        return lieuAdresse;
    }

    public void setLieuAdresse(String lieuAdresse) {
        this.lieuAdresse = lieuAdresse;
    }

    public int getLieuCapacite() {
        return lieuCapacite;
    }

    public void setLieuCapacite(int lieuCapacite) {
        this.lieuCapacite = lieuCapacite;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", lieuId=" + lieuId +
                ", nom='" + nom + '\'' +
                ", dateEvenement=" + dateEvenement +
                ", organisateur='" + organisateur + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
