package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Commentaire;
import models.Publication;
import services.ServiceCommentaire;
import services.ServicePublication;

import java.io.IOException;

public class ShowPublicationController {

    @FXML
    private Label lblTitre;
    @FXML
    private Label lblType;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblContenu;
    @FXML
    private ListView<Commentaire> listCommentaires;
    @FXML
    private TextArea txtNewComment;
    @FXML
    private TextField txtEditTitre;
    @FXML
    private TextArea txtEditContenu;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnPostComment;

    private Publication currentPublication;
    private ServicePublication sp = new ServicePublication();
    private ServiceCommentaire sc = new ServiceCommentaire();
    private ObservableList<Commentaire> obsList = FXCollections.observableArrayList();
    private Commentaire selectedCommentForEdit = null;

    public void initData(Publication p) {
        this.currentPublication = p;
        lblTitre.setText(p.getTitre());
        lblType.setText("Type: " + p.getType());
        lblDate.setText("Publié le: " + p.getDateCreation());
        lblContenu.setText(p.getContenu());

        loadComments();
    }

    private void loadComments() {
        obsList.clear();
        obsList.addAll(sc.getByPublication(currentPublication.getId()));
        listCommentaires.setItems(obsList);
    }

    @FXML
    private void toggleEditPublication(ActionEvent event) {
        boolean isEditing = !txtEditTitre.isVisible();

        // Toggle visibility and management
        lblTitre.setVisible(!isEditing);
        lblTitre.setManaged(!isEditing);
        lblContenu.setVisible(!isEditing);
        lblContenu.setManaged(!isEditing);

        txtEditTitre.setVisible(isEditing);
        txtEditTitre.setManaged(isEditing);
        txtEditContenu.setVisible(isEditing);
        txtEditContenu.setManaged(isEditing);

        btnEdit.setVisible(!isEditing);
        btnEdit.setManaged(!isEditing);
        btnSave.setVisible(isEditing);
        btnSave.setManaged(isEditing);
        btnCancel.setVisible(isEditing);
        btnCancel.setManaged(isEditing);

        if (isEditing) {
            txtEditTitre.setText(currentPublication.getTitre());
            txtEditContenu.setText(currentPublication.getContenu());
        }
    }

    @FXML
    private void savePublicationUpdate(ActionEvent event) {
        String newTitre = txtEditTitre.getText().trim();
        String newContenu = txtEditContenu.getText().trim();

        if (newTitre.isEmpty() || newContenu.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Le titre et le contenu ne peuvent pas être vides.");
            alert.show();
            return;
        }

        currentPublication.setTitre(newTitre);
        currentPublication.setContenu(newContenu);

        sp.update(currentPublication);
        
        // Update Labels
        lblTitre.setText(newTitre);
        lblContenu.setText(newContenu);
        
        // Return to view mode
        toggleEditPublication(event);
    }

    @FXML
    private void postComment(ActionEvent event) {
        String texte = txtNewComment.getText().trim();
        if (texte.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez écrire un commentaire.");
            alert.show();
            return;
        }

        if (selectedCommentForEdit != null) {
            // Update mode
            selectedCommentForEdit.setContenu(texte);
            sc.update(selectedCommentForEdit);
            selectedCommentForEdit = null;
            btnPostComment.setText("Publier");
        } else {
            // Add mode
            Commentaire c = new Commentaire();
            c.setContenu(texte);
            c.setPublicationId(currentPublication.getId());
            c.setAuteurId(1); // active user ID
            sc.add(c);
        }

        txtNewComment.clear();
        loadComments();
    }

    @FXML
    private void editCommentAction(ActionEvent event) {
        selectedCommentForEdit = listCommentaires.getSelectionModel().getSelectedItem();
        if (selectedCommentForEdit == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un commentaire à modifier.");
            alert.show();
            return;
        }

        txtNewComment.setText(selectedCommentForEdit.getContenu());
        btnPostComment.setText("Mettre à jour");
        txtNewComment.requestFocus();
    }

    @FXML
    private void deleteComment(ActionEvent event) {
        Commentaire selected = listCommentaires.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sc.delete(selected);
            loadComments();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/forum.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
