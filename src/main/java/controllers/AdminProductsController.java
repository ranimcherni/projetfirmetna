package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import models.Produit;
import services.ProduitService;
import utils.AlertUtils;

public class AdminProductsController {

    @FXML private Label totalProductsLabel;
    @FXML private Label animauxLabel;
    @FXML private Label vegetauxLabel;
    @FXML private Label bioProductsLabel;
    @FXML private Label totalStockLabel;
    @FXML private Label averagePriceLabel;
    @FXML private Label lowStockLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, Integer> idCol;
    @FXML private TableColumn<Produit, String> imageCol;
    @FXML private TableColumn<Produit, String> nomCol;
    @FXML private TableColumn<Produit, String> typeCol;
    @FXML private TableColumn<Produit, Double> prixCol;
    @FXML private TableColumn<Produit, String> uniteCol;
    @FXML private TableColumn<Produit, Integer> stockCol;
    @FXML private TableColumn<Produit, Boolean> bioCol;
    @FXML private TableColumn<Produit, String> badgeCol;
    @FXML private TableColumn<Produit, Void> actionsCol;

    private final ProduitService produitService = new ProduitService();
    private final ObservableList<Produit> produitList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadProduits();
        setupFilters();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        imageCol.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        uniteCol.setCellValueFactory(new PropertyValueFactory<>("unite"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        bioCol.setCellValueFactory(new PropertyValueFactory<>("bio"));
        badgeCol.setCellValueFactory(new PropertyValueFactory<>("badge"));

        imageCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(56);
                imageView.setFitHeight(42);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    return;
                }
                imageView.setImage(resolveImage(item));
                setGraphic(imageView);
            }
        });

        typeCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label("animale".equalsIgnoreCase(item) ? "Animaux" : "Vegetaux");
                badge.getStyleClass().add("admin-badge-type");
                badge.getStyleClass().add("animale".equalsIgnoreCase(item) ? "bg-blue-light" : "bg-green-light");
                setGraphic(badge);
            }
        });

        bioCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(Boolean.TRUE.equals(item) ? "Bio" : "Standard");
                badge.getStyleClass().add("admin-badge-status");
                badge.getStyleClass().add(Boolean.TRUE.equals(item) ? "bg-success-light" : "bg-purple-light");
                setGraphic(badge);
            }
        });

        prixCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f DT", item));
            }
        });

        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Editer");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("admin-action-btn-edit");
                deleteBtn.getStyleClass().add("admin-action-btn-delete");
                editBtn.setOnAction(event -> openForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadProduits() {
        produitList.setAll(produitService.getAll());
        produitTable.setItems(produitList);
        totalProductsLabel.setText(String.valueOf(produitList.size()));
        animauxLabel.setText(String.valueOf(produitList.stream().filter(p -> "animale".equalsIgnoreCase(p.getType())).count()));
        vegetauxLabel.setText(String.valueOf(produitList.stream().filter(p -> "vegetale".equalsIgnoreCase(p.getType())).count()));
        
        // New stats
        long bioCount = produitList.stream().filter(p -> p.isBio()).count();
        bioProductsLabel.setText(String.valueOf(bioCount));
        
        int totalStock = produitList.stream().mapToInt(Produit::getStock).sum();
        totalStockLabel.setText(String.valueOf(totalStock));
        
        double averagePrice = produitList.stream().mapToDouble(Produit::getPrix).average().orElse(0.0);
        averagePriceLabel.setText(String.format("%.2f TND", averagePrice));
        
        long lowStock = produitList.stream().filter(p -> p.getStock() < 5).count();
        lowStockLabel.setText(String.valueOf(lowStock));
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList("Tous", "vegetale", "animale"));
        typeFilter.getSelectionModel().selectFirst();

        FilteredList<Produit> filteredData = new FilteredList<>(produitList, p -> true);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));
        typeFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));

        SortedList<Produit> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(produitTable.comparatorProperty());
        produitTable.setItems(sortedData);
    }

    private void updateFilter(FilteredList<Produit> filteredData) {
        filteredData.setPredicate(produit -> {
            String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String type = typeFilter.getValue();

            boolean matchesSearch = search.isEmpty()
                    || produit.getNom().toLowerCase().contains(search)
                    || (produit.getDescription() != null && produit.getDescription().toLowerCase().contains(search));

            boolean matchesType = type == null || "Tous".equalsIgnoreCase(type)
                    || produit.getType().equalsIgnoreCase(type);

            return matchesSearch && matchesType;
        });
    }

    @FXML
    private void handleAddProduct() {
        openForm(null);
    }

    private void openForm(Produit produit) {
        ProductFormController controller = AdminLayoutController.getInstance()
                .loadViewWithController("/esprit/tn/fxml/product_form.fxml");
        if (controller != null) {
            controller.configureForAdmin(this::loadProduits);
            if (produit != null) {
                controller.setProduit(produit);
            }
        }
    }

    private void handleDelete(Produit produit) {
        if (AlertUtils.showConfirmation("Suppression", "Supprimer le produit " + produit.getNom() + " ?")) {
            try {
                produitService.delete(produit);
                loadProduits();
                AlertUtils.showSuccess("Suppression", "Produit supprimé avec succès.");
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("foreign key constraint")) {
                    AlertUtils.showError("Impossible de supprimer", "Ce produit est associé à des commandes. Vous devez d'abord supprimer les commandes liées.");
                } else {
                    AlertUtils.showError("Erreur", "Impossible de supprimer le produit: " + errorMsg);
                }
            }
        }
    }

    private Image resolveImage(String imageUrl) {
        try {
            if (imageUrl.startsWith("/")) {
                return new Image(getClass().getResource(imageUrl).toExternalForm(), true);
            }
            return new Image(imageUrl, true);
        } catch (Exception e) {
            return new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm(), true);
        }
    }
}
