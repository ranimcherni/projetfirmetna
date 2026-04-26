package models;

public class Offre {
    private int id;
    private String telephone;
    private String categorie;
    private String description;
    private String photo;
    private int quantite;

    public Offre() {
    }

    public Offre(String telephone, String categorie, String description, String photo, int quantite) {
        this.telephone = telephone;
        this.categorie = categorie;
        this.description = description;
        this.photo = photo;
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}
