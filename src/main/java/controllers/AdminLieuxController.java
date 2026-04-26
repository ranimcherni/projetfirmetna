package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Lieu;
import services.LieuService;
import utils.AlertUtils;

public class AdminLieuxController {

    @FXML private TableView<Lieu> lieuTable;
    @FXML private TableColumn<Lieu, Integer> idCol;
    @FXML private TableColumn<Lieu, String> villeCol;
    @FXML private TableColumn<Lieu, String> adresseCol;
    @FXML private TableColumn<Lieu, Integer> capaciteCol;
    @FXML private TableColumn<Lieu, Boolean> dispoCol;
    @FXML private TableColumn<Lieu, String> imageCol;
    @FXML private TableColumn<Lieu, String> descCol;
    @FXML private TableColumn<Lieu, Void> actionsCol;

    private final LieuService lieuService = new LieuService();
    private final ObservableList<Lieu> lieuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadLieux();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        dispoCol.setCellValueFactory(cd -> {
            Lieu l = cd.getValue();
            boolean v = l != null && l.isDisponibilite();
            return new SimpleObjectProperty<>(v);
        });
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        dispoCol.setCellFactory(column -> new TableCell<Lieu, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Lieu l = getTableRow().getItem();
                    setText(l.isDisponibilite() ? "Oui" : "Non");
                }
            }
        });

        descCol.setCellFactory(column -> new TableCell<Lieu, String>() {
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

        actionsCol.setCellFactory(column -> new TableCell<Lieu, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("admin-action-btn-edit");
                deleteBtn.getStyleClass().add("admin-action-btn-delete");
                pane.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(e -> {
                    Lieu l = getTableView().getItems().get(getIndex());
                    handleDelete(l);
                });
                editBtn.setOnAction(e -> {
                    Lieu l = getTableView().getItems().get(getIndex());
                    handleEdit(l);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadLieux() {
        lieuList.setAll(lieuService.getAll());
        lieuTable.setItems(lieuList);
    }

    private void handleDelete(Lieu l) {
        if (AlertUtils.showConfirmation("Confirmation de suppression",
                "Supprimer le lieu « " + l.getVille() + " » ?")) {
            lieuService.delete(l);
            loadLieux();
        }
    }

    private void handleEdit(Lieu l) {
        showLieuForm(l);
    }

    @FXML
    private void handleAddLieu() {
        showLieuForm(null);
    }

    private void showLieuForm(Lieu lieu) {
        LieuFormController controller = AdminLayoutController.getInstance()
                .loadViewWithController("/esprit/tn/fxml/lieu_form.fxml");
        if (controller != null && lieu != null) {
            controller.setLieu(lieu);
        }
    }
}
