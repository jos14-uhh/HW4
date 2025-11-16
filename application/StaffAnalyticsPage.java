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
 * StaffAnalyticsPage - Displays student activity metrics and participation patterns
 * Helps staff identify struggling students and popular topics
 */
public class StaffAnalyticsPage {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<StudentMetric> analyticsTable;

    public static class StudentMetric {
        private final String userName;
        private final String name;
        private final int questionCount;
        private final int answerCount;
        private final String lastActivity;

        public StudentMetric(String userName, String name, int questionCount, 
                           int answerCount, String lastActivity) {
            this.userName = userName;
            this.name = name;
            this.questionCount = questionCount;
            this.answerCount = answerCount;
            this.lastActivity = lastActivity;
        }

        // Getters and property methods...
    }

    public StaffAnalyticsPage(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Student Activity Analytics");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        createAnalyticsTable();
        refreshAnalytics();
        
        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> new StaffHomePage(databaseHelper, staffUsername).show(primaryStage));
        
        layout.getChildren().addAll(titleLabel, analyticsTable, backBtn);
        
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Analytics");
    }

    private void createAnalyticsTable() {
        // Implementation for analytics table
        analyticsTable = new TableView<>();
        // ... table setup code
    }

    private void refreshAnalytics() {
        try {
            ResultSet rs = databaseHelper.getStudentActivityMetrics();
            // Process and display analytics data
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load analytics: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
