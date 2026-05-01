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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox container = new VBox(5);
                    container.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
                    
                    Label title = new Label(p.getTitre());
                    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1b4332;");
                    
                    HBox meta = new HBox(10);
                    meta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    Label typeBadge = new Label(p.getType().toUpperCase());
                    typeBadge.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2d5a27; -fx-padding: 3 10; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                    
                    Label date = new Label("📅 " + p.getDateCreation().toString());
                    date.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                    
                    meta.getChildren().addAll(typeBadge, date);
                    container.getChildren().addAll(title, meta);
                    
                    setGraphic(container);
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
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/add_publication.fxml", "Ajouter Publication");
    }

    @FXML
    private void viewPublicationAction(ActionEvent event) {
        Publication selected = listPublications.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sÃ¢â€Å“Ã‚Â®lectionner une publication.");
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
        utils.NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }
}