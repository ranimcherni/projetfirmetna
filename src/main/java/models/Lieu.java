package models;

public class Lieu {
    private int id;
    private String adresse;
    private String ville;
    private int capacite;
    private String image;
    private boolean disponibilite;
    private String description;

    public Lieu() {
    }

    public Lieu(String adresse, String ville, int capacite, String image, boolean disponibilite, String description) {
        this.adresse = adresse;
        this.ville = ville;
        this.capacite = capacite;
        this.image = image;
        this.disponibilite = disponibilite;
        this.description = description;
    }

    public Lieu(int id, String adresse, String ville, int capacite, String image, boolean disponibilite,
            String description) {
        this.id = id;
        this.adresse = adresse;
        this.ville = ville;
        this.capacite = capacite;
        this.image = image;
        this.disponibilite = disponibilite;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(boolean disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Lieu{" +
                "id=" + id +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", capacite=" + capacite +
                ", disponibilite=" + disponibilite +
                '}';
    }
}
