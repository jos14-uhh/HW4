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
 * StaffDiscussionBoard - Private discussion board for staff members only
 * Allows staff to coordinate, share insights, and discuss student issues privately
 */
public class StaffDiscussionBoard {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<DiscussionPost> discussionTable;
    private TextField titleField;
    private TextArea contentArea;

    /**
     * Data model for discussion posts
     */
    public static class DiscussionPost {
        private final int id;
        private final String staffId;
        private final String staffName;
        private final String title;
        private final String content;
        private final String date;

        public DiscussionPost(int id, String staffId, String staffName, 
                            String title, String content, String date) {
            this.id = id;
            this.staffId = staffId;
            this.staffName = staffName;
            this.title = title;
            this.content = content;
            this.date = date;
        }

        // Getters and property methods
        public int getId() { return id; }
        public String getStaffId() { return staffId; }
        public String getStaffName() { return staffName; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getDate() { return date; }

        public javafx.beans.property.StringProperty titleProperty() { 
            return new javafx.beans.property.SimpleStringProperty(title); 
        }
        public javafx.beans.property.StringProperty authorProperty() { 
            return new javafx.beans.property.SimpleStringProperty(staffName); 
        }
        public javafx.beans.property.StringProperty dateProperty() { 
            return new javafx.beans.property.SimpleStringProperty(date); 
        }
    }

    public StaffDiscussionBoard(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the staff discussion board
     * @param primaryStage the primary application stage
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
     * Creates a new discussion post
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
     * Refreshes the discussion table
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
     * Displays full content of selected discussion post
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

    private String buildFullPostContent(DiscussionPost post) {
        return String.format(
            "Title: %s\nAuthor: %s\nDate: %s\n\n%s",
            post.getTitle(), post.getStaffName(), post.getDate(), post.getContent()
        );
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
