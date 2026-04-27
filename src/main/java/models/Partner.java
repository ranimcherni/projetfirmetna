package models;

import java.sql.Timestamp;

public class Partner {
    private int id;
    private String name;
    private String type; // e.g., 'Supplier', 'Distributor', 'Donor'
    private String email;
    private String phone;
    private String address;
    private Timestamp createdAt;

    // Default constructor
    public Partner() {
    }

    // Constructor without ID (for creation)
    public Partner(String name, String type, String email, String phone, String address) {
        this.name = name;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    // Constructor with all fields
    public Partner(int id, String name, String type, String email, String phone, String address, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Partner{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
