package services;

import interfaces.IService;
import models.Partner;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerService implements IService<Partner> {

    private Connection cnx;

    public PartnerService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Partner partner) {
        if (cnx == null) {
            System.err.println("No DB Connection");
            return;
        }
        String query = "INSERT INTO partner (name, type, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, partner.getName());
            pst.setString(2, partner.getType());
            pst.setString(3, partner.getEmail());
            pst.setString(4, partner.getPhone());
            pst.setString(5, partner.getAddress());
            pst.executeUpdate();
            System.out.println("Partner Added Successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding partner: " + e.getMessage());
        }
    }

    @Override
    public void update(Partner partner) {
        if (cnx == null) return;
        String query = "UPDATE partner SET name=?, type=?, email=?, phone=?, address=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, partner.getName());
            pst.setString(2, partner.getType());
            pst.setString(3, partner.getEmail());
            pst.setString(4, partner.getPhone());
            pst.setString(5, partner.getAddress());
            pst.setInt(6, partner.getId());
            pst.executeUpdate();
            System.out.println("Partner Updated Successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating partner: " + e.getMessage());
        }
    }

    @Override
    public void delete(Partner partner) {
        if (cnx == null) return;
        String query = "DELETE FROM partner WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, partner.getId());
            pst.executeUpdate();
            System.out.println("Partner Deleted Successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting partner: " + e.getMessage());
        }
    }

    @Override
    public List<Partner> getAll() {
        List<Partner> partners = new ArrayList<>();
        if (cnx == null) return partners;
        String query = "SELECT * FROM partner";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Partner p = new Partner(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at")
                );
                partners.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching partners: " + e.getMessage());
        }
        return partners;
    }

    public boolean isEmailOrPhoneUnique(String email, String phone, int excludeId) {
        if (cnx == null) return false;
        String query = "SELECT COUNT(*) FROM partner WHERE (email = ? OR phone = ?) AND id != ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, phone);
            pst.setInt(3, excludeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // true if unique (0 matches)
            }
        } catch (SQLException e) {
            System.err.println("Error checking uniqueness: " + e.getMessage());
        }
        return false;
    }

    public Partner getPartnerByEmail(String email) {
        if (cnx == null) return null;
        String query = "SELECT * FROM partner WHERE email = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Partner(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching partner by email: " + e.getMessage());
        }
        return null;
    }
}
