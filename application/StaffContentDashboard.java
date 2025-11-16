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
 * StaffContentDashboard - Allows staff to monitor all student questions and answers.
 * Provides comprehensive content viewing with filtering and moderation capabilities.
 *
 * <p>This dashboard is intended for staff and instructor roles and exposes
 * functionality to browse, filter, and inspect content items (questions and answers).
 * It depends on {@link DatabaseHelper#getAllContentForStaff} to retrieve a ResultSet
 * containing unified content rows suitable for administrative review.</p>
 */
public class StaffContentDashboard {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<ContentItem> contentTable;
    private ComboBox<String> contentTypeFilter;

    /**
     * Data model for content items (questions and answers).
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

        /**
         * Constructs a ContentItem used in the staff content table.
         *
         * @param type the content type (e.g., "QUESTION" or "ANSWER")
         * @param id the primary id of the content row
         * @param title the title or preview text
         * @param content the full content body
         * @param author the author's username
         * @param authorName the author's display name
         * @param status content status (e.g., "Resolved" or "Active")
         * @param date creation date/time as a String
         */
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

        /**
         * @return the content type (QUESTION/ANSWER)
         */
        public String getType() { return type; }

        /**
         * @return the content id
         */
        public int getId() { return id; }

        /**
         * @return the content title or preview
         */
        public String getTitle() { return title; }

        /**
         * @return the full content body
         */
        public String getContent() { return content; }

        /**
         * @return the author's username
         */
        public String getAuthor() { return author; }

        /**
         * @return the author's display name
         */
        public String getAuthorName() { return authorName; }

        /**
         * @return the content status label
         */
        public String getStatus() { return status; }

        /**
         * @return the content creation date/time string
         */
        public String getDate() { return date; }

        /**
         * JavaFX property helper for the content type column.
         *
         * @return a StringProperty wrapping the type
         */
        public javafx.beans.property.StringProperty typeProperty() {
            return new javafx.beans.property.SimpleStringProperty(type);
        }

        /**
         * JavaFX property helper for the title/preview column.
         *
         * @return a StringProperty wrapping the title
         */
        public javafx.beans.property.StringProperty titleProperty() {
            return new javafx.beans.property.SimpleStringProperty(title);
        }

        /**
         * JavaFX property helper for the author column.
         *
         * @return a StringProperty wrapping the author's display name
         */
        public javafx.beans.property.StringProperty authorProperty() {
            return new javafx.beans.property.SimpleStringProperty(authorName);
        }

        /**
         * JavaFX property helper for the status column.
         *
         * @return a StringProperty wrapping the status string
         */
        public javafx.beans.property.StringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }
    }

    /**
     * Constructs a StaffContentDashboard.
     *
     * @param databaseHelper the {@link DatabaseHelper} instance for DB access
     * @param staffUsername the username of the staff member using the dashboard
     */
    public StaffContentDashboard(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the content monitoring dashboard on the provided stage.
     *
     * @param primaryStage the primary application {@link Stage}
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
     * Creates and configures the content table columns.
     * Initializes {@link #contentTable} and column cell factories.
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
     * Refreshes the content table with current data from the database.
     * Applies the selected {@link #contentTypeFilter} to limit visible rows.
     * Any SQL errors are shown to the user via an alert dialog.
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
     * Displays a detailed view for the currently selected content item.
     * If no item is selected, a warning is shown to the user.
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
     * Builds a formatted details string for the given content item.
     *
     * @param item the {@link ContentItem} to format
     * @return a human-readable formatted string representing the item details
     */
    private String buildContentDetails(ContentItem item) {
        return String.format(
            "Type: %s\nID: %d\nTitle: %s\nAuthor: %s (%s)\nStatus: %s\nDate: %s\n\nContent:\n%s",
            item.getType(), item.getId(), item.getTitle(), item.getAuthorName(),
            item.getAuthor(), item.getStatus(), item.getDate(), item.getContent()
        );
    }

    /**
     * Shows a modal alert to the user.
     *
     * @param type the {@link Alert.AlertType} to display
     * @param title the dialog window title
     * @param message the message text to display in the dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
