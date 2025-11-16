package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import databasePart1.DatabaseHelper;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * StaffContentDashboard - Allows staff to monitor all student questions and answers
 * Provides comprehensive content viewing with filtering and moderation capabilities
 */
public class StaffContentDashboard {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<ContentItem> contentTable;
    private ComboBox<String> contentTypeFilter;

    /**
     * Data model for content items (questions and answers)
     */
    public static class ContentItem {
        private final String type;
        private final int id;
        private final String title;
        private final String content;
        private final String author;
        private final String authorName;
        private final String status;
        private final String date;

        public ContentItem(String type, int id, String title, String content, 
                          String author, String authorName, String status, String date) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
            this.authorName = authorName;
            this.status = status;
            this.date = date;
        }

        // Getters
        public String getType() { return type; }
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getAuthor() { return author; }
        public String getAuthorName() { return authorName; }
        public String getStatus() { return status; }
        public String getDate() { return date; }

        // Property methods for TableView
        public javafx.beans.property.StringProperty typeProperty() { 
            return new javafx.beans.property.SimpleStringProperty(type); 
        }
        public javafx.beans.property.StringProperty titleProperty() { 
            return new javafx.beans.property.SimpleStringProperty(title); 
        }
        public javafx.beans.property.StringProperty authorProperty() { 
            return new javafx.beans.property.SimpleStringProperty(authorName); 
        }
        public javafx.beans.property.StringProperty statusProperty() { 
            return new javafx.beans.property.SimpleStringProperty(status); 
        }
    }

    /**
     * Constructs a StaffContentDashboard
     * @param databaseHelper database access object
     * @param staffUsername the staff member's username
     */
    public StaffContentDashboard(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the content monitoring dashboard
     * @param primaryStage the primary application stage
     */
    public void show(Stage primaryStage) {
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        Label titleLabel = new Label("Content Monitoring Dashboard");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Controls
        HBox controlsBox = new HBox(10);
        controlsBox.setPadding(new Insets(10, 0, 10, 0));

        contentTypeFilter = new ComboBox<>();
        contentTypeFilter.getItems().addAll("All Content", "Questions Only", "Answers Only");
        contentTypeFilter.setValue("All Content");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshContent());

        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.setOnAction(e -> viewContentDetails());

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> new StaffHomePage(databaseHelper, staffUsername).show(primaryStage));

        controlsBox.getChildren().addAll(
            new Label("Filter:"), contentTypeFilter, refreshBtn, viewDetailsBtn, backBtn
        );

        // Content Table
        createContentTable();

        mainLayout.getChildren().addAll(titleLabel, controlsBox, contentTable);

        // Load initial data
        refreshContent();

        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Content Monitoring");
    }

    /**
     * Creates and configures the content table
     */
    private void createContentTable() {
        contentTable = new TableView<>();
        contentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ContentItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setPrefWidth(100);

        TableColumn<ContentItem, String> titleCol = new TableColumn<>("Title/Preview");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleCol.setPrefWidth(200);

        TableColumn<ContentItem, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        authorCol.setPrefWidth(150);

        TableColumn<ContentItem, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(100);

        contentTable.getColumns().addAll(typeCol, titleCol, authorCol, statusCol);
    }

    /**
     * Refreshes the content table with current data
     */
    private void refreshContent() {
        try {
            ObservableList<ContentItem> content = FXCollections.observableArrayList();
            ResultSet rs = databaseHelper.getAllContentForStaff();
            
            while (rs != null && rs.next()) {
                String type = rs.getString("content_type");
                String filter = contentTypeFilter.getValue();
                
                // Apply filter
                if ("All Content".equals(filter) || 
                    ("Questions Only".equals(filter) && "QUESTION".equals(type)) ||
                    ("Answers Only".equals(filter) && "ANSWER".equals(type))) {
                    
                    ContentItem item = new ContentItem(
                        type,
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getString("userName"),
                        rs.getString("user_name"),
                        rs.getBoolean("resolved") ? "Resolved" : "Active",
                        rs.getTimestamp("request_date").toString()
                    );
                    content.add(item);
                }
            }
            
            contentTable.setItems(content);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                     "Failed to load content: " + e.getMessage());
        }
    }

    /**
     * Displays detailed view of selected content
     */
    private void viewContentDetails() {
        ContentItem selected = contentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a content item to view details.");
            return;
        }

        // Create detailed view dialog
        TextArea detailsArea = new TextArea();
        detailsArea.setText(buildContentDetails(selected));
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefSize(600, 400);

        VBox content = new VBox(10, new Label("Content Details"), detailsArea);
        content.setPadding(new Insets(15));

        Scene detailsScene = new Scene(content, 650, 500);
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Content Details - " + selected.getType());
        detailsStage.setScene(detailsScene);
        detailsStage.show();
    }

    /**
     * Builds detailed content description
     * @param item the content item to describe
     * @return formatted details string
     */
    private String buildContentDetails(ContentItem item) {
        return String.format(
            "Type: %s\nID: %d\nTitle: %s\nAuthor: %s (%s)\nStatus: %s\nDate: %s\n\nContent:\n%s",
            item.getType(), item.getId(), item.getTitle(), item.getAuthorName(), 
            item.getAuthor(), item.getStatus(), item.getDate(), item.getContent()
        );
    }

    /**
     * Shows an alert dialog
     * @param type alert type
     * @param title alert title
     * @param message alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
