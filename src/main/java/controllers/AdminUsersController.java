package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import java.util.List;
import java.util.Optional;
import utils.AlertUtils;

public class AdminUsersController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label farmersLabel;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> statusFilter;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nomCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> typeCol;
    @FXML private TableColumn<User, String> statusCol;
    @FXML private TableColumn<User, String> telCol;
    @FXML private TableColumn<User, java.sql.Timestamp> dateCol;
    @FXML private TableColumn<User, Void> actionsCol;

    private UserService userService = new UserService();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        setupFilters();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        
        // Nom column with initial bubble logic
        nomCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    User u = getTableRow().getItem();
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    String initials = (u.getNom().length() > 0 ? u.getNom().substring(0, 1) : "") + 
                                     (u.getPrenom() != null && u.getPrenom().length() > 0 ? u.getPrenom().substring(0, 1) : "");
                    
                    Label bubble = new Label(initials.toUpperCase());
                    bubble.getStyleClass().add("user-initials-bubble");
                    
                    Label name = new Label(u.getPrenom() + " " + u.getNom());
                    box.getChildren().addAll(bubble, name);
                    setGraphic(box);
                }
            }
        });

        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        telCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));

        // Type column (Badge style)
        typeCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User u = getTableRow().getItem();
                    Label badge = new Label(u.getRole());
                    badge.getStyleClass().add("admin-badge-type");
                    if ("Agriculteur".equals(u.getRole())) badge.getStyleClass().add("bg-green-light");
                    else if ("Client".equals(u.getRole())) badge.getStyleClass().add("bg-blue-light");
                    else badge.getStyleClass().add("bg-purple-light");
                    setGraphic(badge);
                }
            }
        });

        // Status column (Badge style)
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User u = getTableRow().getItem();
                    Label badge = new Label(u.getStatus() != null ? u.getStatus() : "Actif");
                    badge.getStyleClass().add("admin-badge-status");
                    if ("Actif".equals(u.getStatus())) {
                        badge.getStyleClass().add("bg-success-light");
                    } else {
                        badge.getStyleClass().add("status-inactif-red");
                    }
                    setGraphic(badge);
                }
            }
        });

        dateCol.setCellFactory(column -> new TableCell<User, java.sql.Timestamp>() {
            @Override
            protected void updateItem(java.sql.Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDateTime().toLocalDate().toString());
                }
            }
        });

        // Actions Column (Edit/Delete Buttons)
        actionsCol.setCellFactory(column -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("admin-action-btn-edit");
                deleteBtn.getStyleClass().add("admin-action-btn-delete");
                pane.setAlignment(Pos.CENTER);
                
                deleteBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    handleDelete(u);
                });
                
                editBtn.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    handleEdit(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadUsers() {
        userList.setAll(userService.getAll());
        userTable.setItems(userList);
        updateStatistics();
    }

    private void updateStatistics() {
        if (userList.isEmpty()) {
            totalUsersLabel.setText("0");
            activeUsersLabel.setText("0%");
            farmersLabel.setText("0");
            return;
        }

        int total = userList.size();
        int activeCount = 0;
        int farmerCount = 0;

        for (User u : userList) {
            if ("Actif".equalsIgnoreCase(u.getStatus())) activeCount++;
            if ("Agriculteur".equalsIgnoreCase(u.getRole())) farmerCount++;
        }

        int activePercent = (activeCount * 100) / total;

        totalUsersLabel.setText(String.valueOf(total));
        activeUsersLabel.setText(activePercent + "%");
        farmersLabel.setText(String.valueOf(farmerCount));
    }

    private void setupFilters() {
        typeFilter.setItems(FXCollections.observableArrayList("Tous les types", "Agriculteur", "Client", "Donateur"));
        statusFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "Inactif"));
        
        typeFilter.getSelectionModel().select(0);
        statusFilter.getSelectionModel().select(0);

        FilteredList<User> filteredData = new FilteredList<>(userList, p -> true);

        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));
        typeFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));
        statusFilter.valueProperty().addListener((obs, oldV, newV) -> updateFilter(filteredData));

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
    }

    private void updateFilter(FilteredList<User> filteredData) {
        filteredData.setPredicate(user -> {
            String search = searchField.getText().toLowerCase();
            String type = typeFilter.getValue();
            String status = statusFilter.getValue();

            boolean matchesSearch = search.isEmpty() || 
                                    user.getNom().toLowerCase().contains(search) || 
                                    user.getEmail().toLowerCase().contains(search);
            
            boolean matchesType = type.equals("Tous les types") || user.getRole().equals(type);
            boolean matchesStatus = status.equals("Tous les statuts") || 
                                    (user.getStatus() != null && user.getStatus().equals(status));

            return matchesSearch && matchesType && matchesStatus;
        });
    }

    private void handleDelete(User u) {
        if (AlertUtils.showConfirmation("Confirmation de suppression", 
                "Supprimer l'utilisateur " + u.getEmail() + " ?")) {
            userService.delete(u);
            loadUsers();
        }
    }

    private void handleEdit(User u) {
        showUserDialog(u);
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    private void showUserDialog(User user) {
        UserFormController controller = AdminLayoutController.getInstance()
                .loadViewWithController("/esprit/tn/fxml/user_form.fxml");
        
        if (controller != null && user != null) {
            controller.setUser(user);
        }
    }
}
