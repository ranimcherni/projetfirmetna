package models;

public class User {
    private int id;
    private String email;
    private String password;
    private String role;
    private String nom;
    private String prenom;
    private String localisation;
    private String bio;
    private String specialite;
    private String telephone;
    private String status;
    private String image;
    private java.sql.Timestamp registrationDate;

    public User() {
    }

    public User(String email, String password, String role, String nom, String prenom, String localisation, String bio,
            String specialite, String telephone, String image) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.localisation = localisation;
        this.bio = bio;
        this.specialite = specialite;
        this.telephone = telephone;
        this.image = image;
    }

    public User(String email, String password, String role, String nom, String prenom, String localisation, String bio,
            String specialite, String telephone) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.localisation = localisation;
        this.bio = bio;
        this.specialite = specialite;
        this.telephone = telephone;
    }

    public User(String email, String password, String role, String nom, String prenom, String localisation, String bio,
            String specialite) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.localisation = localisation;
        this.bio = bio;
        this.specialite = specialite;
    }

    public User(int id, String email, String password, String role, String nom, String prenom, String localisation,
            String bio, String specialite) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.localisation = localisation;
        this.bio = bio;
        this.specialite = specialite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public java.sql.Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(java.sql.Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
