package models;

import java.sql.Date;

public class Contract {
    private int id;
    private int partnerId;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;
    private double value;
    private String status; // e.g., 'Active', 'Expired', 'Pending'

    // Default constructor
    public Contract() {
    }

    // Constructor without ID (for creation)
    public Contract(int partnerId, String title, String description, Date startDate, Date endDate, double value, String status) {
        this.partnerId = partnerId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.value = value;
        this.status = status;
    }

    // Constructor with all fields
    public Contract(int id, int partnerId, String title, String description, Date startDate, Date endDate, double value, String status) {
        this.id = id;
        this.partnerId = partnerId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.value = value;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", partnerId=" + partnerId +
                ", status='" + status + '\'' +
                '}';
    }
}
