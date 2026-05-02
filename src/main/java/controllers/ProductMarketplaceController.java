package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Produit;
import models.User;
import services.ProduitService;
import utils.AlertUtils;
import utils.NavigationService;
import utils.PanierSession;
import utils.ProductNavigationState;
import utils.UserSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductMarketplaceController {

    private static final int PAGE_SIZE = 8;

    @FXML private Label roleInfoLabel;
    @FXML private Label pageInfoLabel;
    @FXML private Label panierCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button addProductBtn;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private TilePane productsPane;
    @FXML private Label emptyLabel;

    private final ProduitService produitService = new ProduitService();
    private User currentUser;
    private List<Produit> filteredProducts = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    public void initialize() {
        currentUser = UserSession.getInstance().getUser();
        roleInfoLabel.setText(currentUser != null ? "Connecte en tant que: " + currentUser.getRole() : "Visiteur");

        typeFilter.setItems(FXCollections.observableArrayList("tous", "vegetale", "animale"));
        String selectedType = ProductNavigationState.consumeSelectedType();
        if (selectedType == null || selectedType.isBlank()) {
            selectedType = "tous";
        }
        typeFilter.setValue(selectedType.toLowerCase());

        boolean isAgriculteur = currentUser != null && "Agriculteur".equalsIgnoreCase(currentUser.getRole());
        addProductBtn.setVisible(isAgriculteur);
        addProductBtn.setManaged(isAgriculteur);

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            currentPage = 0;
            refreshProducts();
        });
        typeFilter.valueProperty().addListener((obs, oldV, newV) -> {
            currentPage = 0;
            refreshProducts();
        });

        refreshProducts();
        refreshPanierCount();
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front.fxml", "Accueil");
    }

    @FXML
    private void handleForum(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/forum.fxml", "Forum Communautaire");
    }

    @FXML
    private void handleEvenements(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_evenements.fxml", "Événements");
    }

    @FXML
    private void handleDons(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/front_donations_offres.fxml", "Donations & Solidarité");
    }

    @FXML
    private void handlePartenariats(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/FrontPartnerView.fxml", "Espace Partenaires");
    }

    @FXML
    private void handleProfil(javafx.event.ActionEvent event) {
        NavigationService.switchScene(event, "/esprit/tn/fxml/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        NavigationService.switchScene(event, "/esprit/tn/fxml/home.fxml", "Bienvenue");
    }

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/product_form.fxml"));
            Parent root = loader.load();
            ProductFormController controller = loader.getController();
            controller.configureForMarketplace(this::refreshProducts);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter un produit");
            stage.setScene(new Scene(root, 900, 700));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire produit.");
        }
    }

    @FXML
    private void handlePanier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/panier.fxml"));
            Parent root = loader.load();
            PanierController controller = loader.getController();
            controller.setOnCheckoutCompleted(() -> {
                refreshPanierCount();
                refreshProducts();
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Panier");
            stage.setScene(new Scene(root, 760, 520));
            stage.showAndWait();
            refreshPanierCount();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le panier.");
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    private void handleNextPage() {
        if ((currentPage + 1) * PAGE_SIZE < filteredProducts.size()) {
            currentPage++;
            renderCurrentPage();
        }
    }

    private void refreshProducts() {
        String selectedType = typeFilter.getValue();
        if (selectedType == null || selectedType.isBlank()) {
            selectedType = "tous";
            typeFilter.setValue(selectedType);
        }

        List<Produit> produits = produitService.getByType(selectedType);
        String search = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        filteredProducts = produits.stream()
                .filter(p -> search.isEmpty()
                        || p.getNom().toLowerCase().contains(search)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(search)))
                .toList();

        renderCurrentPage();
    }

    private void renderCurrentPage() {
        productsPane.getChildren().clear();
        emptyLabel.setVisible(filteredProducts.isEmpty());
        emptyLabel.setManaged(filteredProducts.isEmpty());

        int totalPages = Math.max(1, (int) Math.ceil(filteredProducts.size() / (double) PAGE_SIZE));
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filteredProducts.size());
        List<Produit> pageItems = filteredProducts.isEmpty() ? List.of() : filteredProducts.subList(from, to);

        for (Produit produit : pageItems) {
            productsPane.getChildren().add(buildCard(produit));
        }

        pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + totalPages + " — " + filteredProducts.size() + " produit(s)");
        prevPageBtn.setDisable(currentPage == 0);
        nextPageBtn.setDisable((currentPage + 1) >= totalPages);
    }

    private VBox buildCard(Produit produit) {
        VBox card = new VBox(14);
        card.setPrefWidth(280);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("product-card");

        ImageView imageView = new ImageView(resolveImage(produit.getImageUrl()));
        imageView.setFitWidth(246);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("product-card-image");

        Label type = new Label("animale".equalsIgnoreCase(produit.getType()) ? "Animaux" : "Vegetaux");
        type.getStyleClass().add("product-card-type");

        Label name = new Label(produit.getNom());
        name.getStyleClass().add("product-card-title");
        name.setWrapText(true);

        Label description = new Label(produit.getDescription());
        description.setWrapText(true);
        description.getStyleClass().add("product-card-description");

        Label price = new Label(String.format("%.2f DT / %s", produit.getPrix(), produit.getUnite()));
        price.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label stock = new Label(produit.getStock() == 0 ? "Épuisé" : "Stock: " + produit.getStock());
        stock.setStyle(produit.getStock() == 0 ? "-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-weight: bold;" : "-fx-text-fill: #636e72; -fx-font-size: 12;");

        Button detailsBtn = new Button("Voir détails");
        detailsBtn.getStyleClass().addAll("marketplace-button", "marketplace-button-secondary");
        detailsBtn.setOnAction(event -> openDetails(produit));

        Button cartBtn = new Button(produit.getStock() == 0 ? "Épuisé" : "Panier");
        cartBtn.getStyleClass().addAll("marketplace-button", produit.getStock() == 0 ? "marketplace-button-disabled" : "marketplace-button-primary");
        cartBtn.setDisable(currentUser == null || "ADMIN".equalsIgnoreCase(currentUser.getRole()) || produit.getStock() == 0);
        cartBtn.setOnAction(event -> handleAddToCart(produit));

        HBox actions = new HBox(10, detailsBtn, cartBtn);
        actions.getStyleClass().add("product-card-footer");
        VBox.setVgrow(description, Priority.ALWAYS);
        card.getChildren().addAll(imageView, type, name, description, price, stock, actions);
        return card;
    }

    private void openDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/product_detail.fxml"));
            Parent root = loader.load();
            ProductDetailController controller = loader.getController();
            controller.setProduit(produit, () -> handleAddToCart(produit));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root, 880, 500);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les details du produit.");
        }
    }

    private void handleAddToCart(Produit produit) {
        if (currentUser == null) {
            AlertUtils.showError("Connexion requise", "Vous devez etre connecte pour commander.");
            return;
        }
        if (produit.getStock() <= 0) {
            AlertUtils.showError("Stock insuffisant", "Ce produit est actuellement epuise.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/quantity_dialog.fxml"));
            Parent root = loader.load();
            QuantityDialogController controller = loader.getController();
            controller.setProduit(produit, () -> {
                int quantity = controller.getSelectedQuantity();
                PanierSession.getInstance().addProduit(produit, quantity);
                refreshPanierCount();
                AlertUtils.showSuccess("Panier", quantity + " x " + produit.getNom() + " ajoute(s) au panier.");
            });

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root, 400, 250);
            scene.setFill(null);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le dialogue de quantite.");
        }
    }

    private void refreshPanierCount() {
        panierCountLabel.setText(String.valueOf(PanierSession.getInstance().getNombreArticles()));
    }

    private Image resolveImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/")) {
                return new Image(getClass().getResource(imageUrl).toExternalForm(), true);
            }
            if (imageUrl != null && !imageUrl.isBlank()) {
                return new Image(imageUrl, true);
            }
        } catch (Exception ignored) {
        }
        return new Image(getClass().getResource("/esprit/tn/images/logo1.png").toExternalForm(), true);
    }

    @FXML
    private void handleOpenChatbot(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esprit/tn/fxml/chatbot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Chatbot Firmetna");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
