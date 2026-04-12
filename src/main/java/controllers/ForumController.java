package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Publication;
import services.ServicePublication;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController implements Initializable {

    @FXML
    private ListView<Publication> listPublications;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> comboFilter;

    private ServicePublication sp = new ServicePublication();
    private ObservableList<Publication> obsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboFilter.getItems().addAll("Toutes", "Discussion", "Question", "Annonce");
        comboFilter.setValue("Toutes");

        loadData();

        listPublications.setCellFactory(param -> new ListCell<Publication>() {
            @Override
            protected void updateItem(Publication p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(p.getTitre() + " - " + p.getType() + " (" + p.getDateCreation() + ")");
                    // TODO: advanced custom graphics (cards) can go here
                }
            }
        });

        // Search logic
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterList(newValue, comboFilter.getValue());
        });

        comboFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterList(searchField.getText(), newValue);
        });
    }

    private void loadData() {
        obsList.clear();
        obsList.addAll(sp.getAll());
        listPublications.setItems(obsList);
    }

    private void filterList(String keyword, String type) {
        List<Publication> items = sp.search(keyword);
        obsList.clear();

        for (Publication p : items) {
            if ("Toutes".equals(type) || p.getType().equals(type)) {
                obsList.add(p);
            }
        }
        listPublications.setItems(obsList);
    }

    @FXML
    private void switchToAddPublication(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/add_publication.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewPublicationAction(ActionEvent event) {
        Publication selected = listPublications.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner une publication.");
            alert.show();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/show_publication.fxml"));
            Parent root = loader.load();

            ShowPublicationController controller = loader.getController();
            controller.initData(selected);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deletePublicationAction(ActionEvent event) {
        Publication selected = listPublications.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sp.delete(selected);
            loadData(); // refresh list
        }
    }

    @FXML
    private void returnToHome(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/esprit/tn/fxml/home.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // might be missing home.fxml currently, print
            System.err.println("home.fxml not found. Returning to Admin/Front?");
        }
    }
}
