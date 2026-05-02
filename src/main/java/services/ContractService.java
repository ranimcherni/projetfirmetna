package services;

import interfaces.IService;
import models.Contract;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractService implements IService<Contract> {

    private Connection cnx;

    public ContractService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Contract contract) {
        if (cnx == null) return;
        String query = "INSERT INTO contract (partner_id, title, description, start_date, end_date, value, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, contract.getPartnerId());
            pst.setString(2, contract.getTitle());
            pst.setString(3, contract.getDescription());
            pst.setDate(4, contract.getStartDate());
            pst.setDate(5, contract.getEndDate());
            pst.setDouble(6, contract.getValue());
            pst.setString(7, contract.getStatus());
            pst.executeUpdate();
            System.out.println("Contract Added Successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding contract: " + e.getMessage());
        }
    }

    @Override
    public void update(Contract contract) {
        if (cnx == null) return;
        String query = "UPDATE contract SET partner_id=?, title=?, description=?, start_date=?, end_date=?, value=?, status=? WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, contract.getPartnerId());
            pst.setString(2, contract.getTitle());
            pst.setString(3, contract.getDescription());
            pst.setDate(4, contract.getStartDate());
            pst.setDate(5, contract.getEndDate());
            pst.setDouble(6, contract.getValue());
            pst.setString(7, contract.getStatus());
            pst.setInt(8, contract.getId());
            pst.executeUpdate();
            System.out.println("Contract Updated Successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating contract: " + e.getMessage());
        }
    }

    @Override
    public void delete(Contract contract) {
        if (cnx == null) return;
        String query = "DELETE FROM contract WHERE id=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, contract.getId());
            pst.executeUpdate();
            System.out.println("Contract Deleted Successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting contract: " + e.getMessage());
        }
    }

    @Override
    public List<Contract> getAll() {
        List<Contract> contracts = new ArrayList<>();
        if (cnx == null) return contracts;
        String query = "SELECT * FROM contract";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                Contract c = new Contract(
                        rs.getInt("id"),
                        rs.getInt("partner_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDouble("value"),
                        rs.getString("status")
                );
                contracts.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contracts: " + e.getMessage());
        }
        return contracts;
    }

    public List<Contract> getContractsByPartnerId(int partnerId) {
        List<Contract> contracts = new ArrayList<>();
        if (cnx == null) return contracts;
        String query = "SELECT * FROM contract WHERE partner_id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, partnerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                contracts.add(new Contract(
                        rs.getInt("id"),
                        rs.getInt("partner_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date"),
                        rs.getDouble("value"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching contracts by partner id: " + e.getMessage());
        }
        return contracts;
    }
}
