package services;

import interfaces.IService;
import models.Publication;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePublication implements IService<Publication> {

    private Connection cnx;

    public ServicePublication() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Publication p) {
        String qry = "INSERT INTO `publication`(`titre`, `contenu`, `type`, `auteur_id`, `image_path`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, p.getTitre());
            pstm.setString(2, p.getContenu());
            pstm.setString(3, p.getType() != null ? p.getType() : "Discussion");
            pstm.setInt(4, p.getAuteurId());
            pstm.setString(5, p.getImagePath());

            pstm.executeUpdate();
            System.out.println("Publication added successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Publication p) {
        String qry = "UPDATE `publication` SET `titre`=?,`contenu`=?,`type`=?,`image_path`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, p.getTitre());
            pstm.setString(2, p.getContenu());
            pstm.setString(3, p.getType());
            pstm.setString(4, p.getImagePath());
            pstm.setInt(5, p.getId());

            pstm.executeUpdate();
            System.out.println("Publication updated successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Publication p) {
        String qry = "DELETE FROM `publication` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, p.getId());

            pstm.executeUpdate();
            System.out.println("Publication deleted successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Publication> getAll() {
        List<Publication> publications = new ArrayList<>();
        String qry = "SELECT * FROM `publication` ORDER BY date_creation DESC";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Publication p = new Publication();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setType(rs.getString("type"));
                p.setDateCreation(rs.getTimestamp("date_creation"));
                p.setAuteurId(rs.getInt("auteur_id"));
                p.setImagePath(rs.getString("image_path"));

                publications.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return publications;
    }

    public List<Publication> search(String keyword) {
        List<Publication> publications = new ArrayList<>();
        String qry = "SELECT * FROM `publication` WHERE titre LIKE ? OR contenu LIKE ? ORDER BY date_creation DESC";

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, "%" + keyword + "%");
            pstm.setString(2, "%" + keyword + "%");
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Publication p = new Publication();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                p.setType(rs.getString("type"));
                p.setDateCreation(rs.getTimestamp("date_creation"));
                p.setAuteurId(rs.getInt("auteur_id"));
                p.setImagePath(rs.getString("image_path"));

                publications.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return publications;
    }
}