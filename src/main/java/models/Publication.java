package models;

import java.sql.Timestamp;

public class Publication {
    private int id;
    private String titre;
    private String contenu;
    private String type;
    private Timestamp dateCreation;
    private int auteurId;
    private String imagePath;
    private String pdfPath;

    public Publication() {
    }

    public Publication(String titre, String contenu, String type, int auteurId, String imagePath, String pdfPath) {
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.auteurId = auteurId;
        this.imagePath = imagePath;
        this.pdfPath = pdfPath;
    }

    public Publication(int id, String titre, String contenu, String type, Timestamp dateCreation, int auteurId, String imagePath, String pdfPath) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
        this.dateCreation = dateCreation;
        this.auteurId = auteurId;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
