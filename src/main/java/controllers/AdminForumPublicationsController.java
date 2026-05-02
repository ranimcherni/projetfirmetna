package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.Publication;
import models.User;
import services.ServicePublication;
import services.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminForumPublicationsController implements Initializable {

    @FXML private TableView<Publication> publicationTable;
    @FXML private TableColumn<Publication, Integer> idCol;
    @FXML private TableColumn<Publication, String> titreCol;
    @FXML private TableColumn<Publication, String> auteurCol;
    @FXML private TableColumn<Publication, String> typeCol;
    @FXML private TableColumn<Publication, String> dateCol;
    @FXML private TableColumn<Publication, Void> actionsCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;

    private ServicePublication sp = new ServicePublication();
    private UserService us = new UserService();
    private ObservableList<Publication> masterList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        titreCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitre()));
        
        typeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        typeCol.setCellFactory(column -> {
            return new TableCell<Publication, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Label badge = new Label(item.toUpperCase());
                        badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                        
                        if (item.equalsIgnoreCase("Discussion")) {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #e6fffa; -fx-text-fill: #27ae60;");
                        } else if (item.equalsIgnoreCase("Question")) {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #ebf8ff; -fx-text-fill: #2980b9;");
                        } else if (item.equalsIgnoreCase("Annonce")) {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #faf5ff; -fx-text-fill: #8e44ad;");
                        } else {
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #f1f2f6; -fx-text-fill: #636e72;");
                        }
                        
                        setGraphic(badge);
                        setText(null);
                    }
                }
            };
        });
        dateCol.setCellValueFactory(cellData -> new SimpleStringProperty(
            cellData.getValue().getDateCreation() != null ? cellData.getValue().getDateCreation().toString().substring(0,16) : ""
        ));

        // Custom Author column to show name instead of ID
        auteurCol.setCellValueFactory(cellData -> {
            User author = us.getUserById(cellData.getValue().getAuteurId());
            String name = (author != null) ? (author.getPrenom() + " " + author.getNom()) : "ID: " + cellData.getValue().getAuteurId();
            return new SimpleStringProperty(name);
        });

        addActionsToTable();
        loadData();

        typeFilter.getItems().addAll("Toutes", "Discussion", "Question", "Annonce");
        typeFilter.setValue("Toutes");

        // Listeners for filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadData() {
        masterList.setAll(sp.getAll());
        publicationTable.setItems(masterList);
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();
        String type = typeFilter.getValue();

        List<Publication> filtered = masterList.stream()
            .filter(p -> p.getTitre().toLowerCase().contains(query) || String.valueOf(p.getAuteurId()).contains(query))
            .filter(p -> type.equals("Toutes") || p.getType().equals(type))
            .collect(Collectors.toList());

        publicationTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addActionsToTable() {
        Callback<TableColumn<Publication, Void>, TableCell<Publication, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Publication, Void> call(final TableColumn<Publication, Void> param) {
                return new TableCell<>() {
                    private final Button btnDelete = new Button("Supprimer");

                    {
                        btnDelete.getStyleClass().add("admin-action-btn");
                        btnDelete.setOnAction(event -> {
                            Publication p = getTableView().getItems().get(getIndex());
                            handleDelete(p);
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

    private void handleDelete(Publication p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la publication : " + p.getTitre());
        alert.setContentText("Êtes-vous sûr ? Cette action supprimera également tous les commentaires associés.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            sp.delete(p);
            loadData();
        }
    }
}
