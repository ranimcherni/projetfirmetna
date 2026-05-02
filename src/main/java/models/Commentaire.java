package models;

import java.sql.Timestamp;

public class Commentaire {
    private int id;
    private String contenu;
    private Timestamp dateCreation;
    private int auteurId;
    private int publicationId;
    private Integer parentId;

    public Commentaire() {
    }

    public Commentaire(String contenu, int auteurId, int publicationId, Integer parentId) {
        this.contenu = contenu;
        this.auteurId = auteurId;
        this.publicationId = publicationId;
        this.parentId = parentId;
    }

    public Commentaire(int id, String contenu, Timestamp dateCreation, int auteurId, int publicationId, Integer parentId) {
        this.id = id;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
        this.auteurId = auteurId;
        this.publicationId = publicationId;
        this.parentId = parentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }

    public int getPublicationId() { return publicationId; }
    public void setPublicationId(int publicationId) { this.publicationId = publicationId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", publicationId=" + publicationId +
                '}';
    }
}
