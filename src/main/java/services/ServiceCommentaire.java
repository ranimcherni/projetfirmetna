package services;

import interfaces.IService;
import models.Commentaire;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommentaire implements IService<Commentaire> {

    private Connection cnx;

    public ServiceCommentaire() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Commentaire c) {
        String qry = "INSERT INTO `commentaire`(`contenu`, `auteur_id`, `publication_id`, `parent_id`) VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, c.getContenu());
            pstm.setInt(2, c.getAuteurId());
            pstm.setInt(3, c.getPublicationId());
            if (c.getParentId() != null) {
                pstm.setInt(4, c.getParentId());
            } else {
                pstm.setNull(4, Types.INTEGER);
            }

            pstm.executeUpdate();
            System.out.println("Commentaire added successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Commentaire c) {
        String qry = "UPDATE `commentaire` SET `contenu`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, c.getContenu());
            pstm.setInt(2, c.getId());

            pstm.executeUpdate();
            System.out.println("Commentaire updated successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Commentaire c) {
        String qry = "DELETE FROM `commentaire` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, c.getId());

            pstm.executeUpdate();
            System.out.println("Commentaire deleted successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Commentaire> getAll() {
        List<Commentaire> commentaires = new ArrayList<>();
        String qry = "SELECT * FROM `commentaire` ORDER BY date_creation ASC";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateCreation(rs.getTimestamp("date_creation"));
                c.setAuteurId(rs.getInt("auteur_id"));
                c.setPublicationId(rs.getInt("publication_id"));
                
                int parentId = rs.getInt("parent_id");
                c.setParentId(rs.wasNull() ? null : parentId);

                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return commentaires;
    }

    public List<Commentaire> getByPublication(int publicationId) {
        List<Commentaire> commentaires = new ArrayList<>();
        String qry = "SELECT * FROM `commentaire` WHERE publication_id = ? ORDER BY date_creation ASC";

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, publicationId);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                c.setDateCreation(rs.getTimestamp("date_creation"));
                c.setAuteurId(rs.getInt("auteur_id"));
                c.setPublicationId(rs.getInt("publication_id"));
                
                int parentId = rs.getInt("parent_id");
                c.setParentId(rs.wasNull() ? null : parentId);

                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return commentaires;
    }
}
