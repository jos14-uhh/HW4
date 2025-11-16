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
 * StaffDiscussionBoard provides a private discussion board UI where staff members
 * can create, view, and manage discussion posts. Posts are stored in the database
 * and are visible only to staff and instructors.
 *
 * <p>This class offers basic CRUD-style behavior (create + read) exposed via a
 * JavaFX interface. It is intended to support Staff-role user stories in HW4
 * and can be extended to include edit/delete features and richer moderation tools.</p>
 */
public class StaffDiscussionBoard {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<DiscussionPost> discussionTable;
    private TextField titleField;
    private TextArea contentArea;

    /**
     * Data model representing a single discussion post in the staff board.
     */
    public static class DiscussionPost {
        private final int id;
        private final String staffId;
        private final String staffName;
        private final String title;
        private final String content;
        private final String date;

        /**
         * Constructs a DiscussionPost model instance.
         *
         * @param id the unique identifier for the post
         * @param staffId the staff member's id who authored the post
         * @param staffName the staff member's display name
         * @param title the post title
         * @param content the full post content
         * @param date the creation date/time as a string
         */
        public DiscussionPost(int id, String staffId, String staffName,
                              String title, String content, String date) {
            this.id = id;
            this.staffId = staffId;
            this.staffName = staffName;
            this.title = title;
            this.content = content;
            this.date = date;
        }

        /**
         * @return the post id
         */
        public int getId() { return id; }

        /**
         * @return the author staff id
         */
        public String getStaffId() { return staffId; }

        /**
         * @return the author display name
         */
        public String getStaffName() { return staffName; }

        /**
         * @return the post title
         */
        public String getTitle() { return title; }

        /**
         * @return the post content
         */
        public String getContent() { return content; }

        /**
         * @return the post creation date/time (string)
         */
        public String getDate() { return date; }

        /**
         * JavaFX property helper for the title column.
         *
         * @return property wrapping the title
         */
        public javafx.beans.property.StringProperty titleProperty() {
            return new javafx.beans.property.SimpleStringProperty(title);
        }

        /**
         * JavaFX property helper for the author column.
         *
         * @return property wrapping the author name
         */
        public javafx.beans.property.StringProperty authorProperty() {
            return new javafx.beans.property.SimpleStringProperty(staffName);
        }

        /**
         * JavaFX property helper for the date column.
         *
         * @return property wrapping the post date
         */
        public javafx.beans.property.StringProperty dateProperty() {
            return new javafx.beans.property.SimpleStringProperty(date);
        }
    }

    /**
     * Constructs the StaffDiscussionBoard UI helper.
     *
     * @param databaseHelper the {@link DatabaseHelper} used to persist and read discussion posts
     * @param staffUsername the username of the staff member currently using the board
     */
    public StaffDiscussionBoard(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Shows the staff discussion board UI on the provided {@link Stage}.
     * Builds the layout, initializes controls, and loads existing posts.
     *
     * @param primaryStage the main application stage on which to display the board
     */
    public void show(Stage primaryStage) {
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("Staff Discussion Board");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // New Post Section
        VBox newPostSection = new VBox(10);
        newPostSection.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-padding: 15;");

        Label newPostLabel = new Label("Create New Discussion Post");
        newPostLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        titleField = new TextField();
        titleField.setPromptText("Enter post title...");

        contentArea = new TextArea();
        contentArea.setPromptText("Enter your discussion content here...");
        contentArea.setPrefHeight(100);

        Button postBtn = new Button("Post Discussion");
        postBtn.setOnAction(e -> createNewPost());

        newPostSection.getChildren().addAll(newPostLabel, titleField, contentArea, postBtn);

        // Discussion Table
        createDiscussionTable();

        // Controls
        HBox controlsBox = new HBox(10);
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshDiscussions());

        Button viewPostBtn = new Button("View Full Post");
        viewPostBtn.setOnAction(e -> viewFullPost());

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> new StaffHomePage(databaseHelper, staffUsername).show(primaryStage));

        controlsBox.getChildren().addAll(refreshBtn, viewPostBtn, backBtn);

        mainLayout.getChildren().addAll(titleLabel, newPostSection, discussionTable, controlsBox);

        refreshDiscussions();

        Scene scene = new Scene(mainLayout, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Discussion Board");
    }

    /**
     * Create and configure the discussion table columns.
     * This method initializes {@link #discussionTable} and sets up cell factories.
     */
    private void createDiscussionTable() {
        discussionTable = new TableView<>();
        discussionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DiscussionPost, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleCol.setPrefWidth(300);

        TableColumn<DiscussionPost, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        authorCol.setPrefWidth(150);

        TableColumn<DiscussionPost, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        dateCol.setPrefWidth(150);

        discussionTable.getColumns().addAll(titleCol, authorCol, dateCol);
    }

    /**
     * Create a new discussion post from the contents of the UI fields.
     * Validates input and persists the post via {@link DatabaseHelper#addStaffDiscussion}.
     * Shows an alert on success or failure.
     */
    private void createNewPost() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                     "Please provide both title and content for your post.");
            return;
        }

        try {
            boolean success = databaseHelper.addStaffDiscussion(staffUsername, title, content);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                         "Your discussion post has been added successfully.");
                titleField.clear();
                contentArea.clear();
                refreshDiscussions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                         "Failed to create discussion post. Please try again.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                     "Error creating post: " + e.getMessage());
        }
    }

    /**
     * Reloads discussion posts from the database and updates the table.
     * Any SQL errors are caught and reported to the user via an alert.
     */
    private void refreshDiscussions() {
        try {
            ObservableList<DiscussionPost> posts = FXCollections.observableArrayList();
            ResultSet rs = databaseHelper.getStaffDiscussions();

            while (rs != null && rs.next()) {
                DiscussionPost post = new DiscussionPost(
                    rs.getInt("id"),
                    rs.getString("staff_id"),
                    rs.getString("staff_name"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_date").toString()
                );
                posts.add(post);
            }

            discussionTable.setItems(posts);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                     "Failed to load discussions: " + e.getMessage());
        }
    }

    /**
     * Shows the full content of the currently selected discussion post in a new window.
     * If no post is selected, a warning is shown.
     */
    private void viewFullPost() {
        DiscussionPost selected = discussionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                     "Please select a discussion post to view.");
            return;
        }

        TextArea fullContent = new TextArea();
        fullContent.setText(buildFullPostContent(selected));
        fullContent.setEditable(false);
        fullContent.setWrapText(true);
        fullContent.setPrefSize(500, 300);

        VBox content = new VBox(10, new Label("Discussion Post Details"), fullContent);
        content.setPadding(new Insets(15));

        Scene detailsScene = new Scene(content, 550, 400);
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Discussion: " + selected.getTitle());
        detailsStage.setScene(detailsScene);
        detailsStage.show();
    }

    /**
     * Builds the formatted content string for a discussion post shown in the details view.
     *
     * @param post the {@link DiscussionPost} to format
     * @return a human-readable formatted string representing the post details
     */
    private String buildFullPostContent(DiscussionPost post) {
        return String.format(
            "Title: %s\nAuthor: %s\nDate: %s\n\n%s",
            post.getTitle(), post.getStaffName(), post.getDate(), post.getContent()
        );
    }

    /**
     * Utility helper to show modal alerts to the user.
     *
     * @param type the type of the alert (information, warning, error, etc.)
     * @param title the alert window title
     * @param message the message body to show in the alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
