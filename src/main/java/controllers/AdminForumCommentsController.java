package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Commentaire;
import models.User;
import services.ServiceCommentaire;
import services.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminForumCommentsController implements Initializable {

    @FXML private TableView<Commentaire> commentTable;
    @FXML private TableColumn<Commentaire, Integer> idCol;
    @FXML private TableColumn<Commentaire, String> auteurCol;
    @FXML private TableColumn<Commentaire, String> contenuCol;
    @FXML private TableColumn<Commentaire, String> dateCol;
    @FXML private TableColumn<Commentaire, Void> actionsCol;

    @FXML private TextField searchField;

    private ServiceCommentaire sc = new ServiceCommentaire();
    private UserService us = new UserService();
    private ObservableList<Commentaire> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        contenuCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContenu()));
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getDateCreation() != null ? cellData.getValue().getDateCreation().toString().substring(0,16) : ""
        ));

        // Custom Author column
        auteurCol.setCellValueFactory(cellData -> {
            User author = us.getUserById(cellData.getValue().getAuteurId());
            String name = (author != null) ? (author.getPrenom() + " " + author.getNom()) : "ID: " + cellData.getValue().getAuteurId();
            return new SimpleStringProperty(name);
        });

        addActionsToTable();
        loadData();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadData() {
        masterList.setAll(sc.getAll());
        commentTable.setItems(masterList);
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();

        List<Commentaire> filtered = masterList.stream()
            .filter(c -> c.getContenu().toLowerCase().contains(query) || String.valueOf(c.getAuteurId()).contains(query))
            .collect(Collectors.toList());

        commentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addActionsToTable() {
        Callback<TableColumn<Commentaire, Void>, TableCell<Commentaire, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Commentaire, Void> call(final TableColumn<Commentaire, Void> param) {
                return new TableCell<>() {
                    private final Button btnDelete = new Button("Supprimer");

                    {
                        btnDelete.getStyleClass().add("admin-action-btn");
                        btnDelete.setOnAction(event -> {
                            Commentaire c = getTableView().getItems().get(getIndex());
                            handleDelete(c);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox container = new HBox(10, btnDelete);
                            container.setAlignment(javafx.geometry.Pos.CENTER);
                            setGraphic(container);
                        }
                    }
                };
            }
        };
        actionsCol.setCellFactory(cellFactory);
    }

    private void handleDelete(Commentaire c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer ce commentaire ?");
        alert.setContentText("\"" + (c.getContenu().length() > 50 ? c.getContenu().substring(0, 47) + "..." : c.getContenu()) + "\"");

        if (alert.showAndWait().get() == ButtonType.OK) {
            sc.delete(c);
            loadData();
        }
    }
}
