package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Evenement;
import services.EvenementService;
import utils.AlertUtils;

public class AdminEvenementsController {

    @FXML private TableView<Evenement> evenementTable;
    @FXML private TableColumn<Evenement, Integer> idCol;
    @FXML private TableColumn<Evenement, String> nomCol;
    @FXML private TableColumn<Evenement, java.sql.Date> dateCol;
    @FXML private TableColumn<Evenement, String> villeCol;
    @FXML private TableColumn<Evenement, String> adresseCol;
    @FXML private TableColumn<Evenement, String> orgCol;
    @FXML private TableColumn<Evenement, String> descCol;
    @FXML private TableColumn<Evenement, Void> actionsCol;

    private final EvenementService evenementService = new EvenementService();
    private final ObservableList<Evenement> evenementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadEvenements();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateEvenement"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("lieuVille"));
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("lieuAdresse"));
        orgCol.setCellValueFactory(new PropertyValueFactory<>("organisateur"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        dateCol.setCellFactory(column -> new TableCell<Evenement, java.sql.Date>() {
            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDate().toString());
                }
            }
        });

        descCol.setCellFactory(column -> new TableCell<Evenement, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    String d = getTableRow().getItem().getDescription();
                    if (d == null) {
                        setText("");
                    } else {
                        setText(d.length() > 80 ? d.substring(0, 77) + "…" : d);
                    }
                }
            }
        });

        actionsCol.setCellFactory(column -> new TableCell<Evenement, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("admin-action-btn-edit");
                deleteBtn.getStyleClass().add("admin-action-btn-delete");
                pane.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    handleDelete(ev);
                });
                editBtn.setOnAction(e -> {
                    Evenement ev = getTableView().getItems().get(getIndex());
                    handleEdit(ev);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadEvenements() {
        evenementList.setAll(evenementService.getAll());
        evenementTable.setItems(evenementList);
    }

    private void handleDelete(Evenement e) {
        if (AlertUtils.showConfirmation("Confirmation de suppression",
                "Supprimer l'événement « " + e.getNom() + " » ?")) {
            evenementService.delete(e);
            loadEvenements();
        }
    }

    private void handleEdit(Evenement e) {
        showEvenementForm(e);
    }

    @FXML
    private void handleAddEvenement() {
        showEvenementForm(null);
    }

    private void showEvenementForm(Evenement ev) {
        EvenementFormController controller = AdminLayoutController.getInstance()
                .loadViewWithController("/esprit/tn/fxml/evenement_form.fxml");
        if (controller != null && ev != null) {
            controller.setEvenement(ev);
        }
    }
}
