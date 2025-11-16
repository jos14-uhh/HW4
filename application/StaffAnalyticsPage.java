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
 * StaffAnalyticsPage displays student activity metrics and participation patterns.
 * The page helps staff identify students who may be struggling and highlights
 * popular topics or trends in questions and answers.
 *
 * <p>This class relies on {@link DatabaseHelper#getStudentActivityMetrics} to
 * obtain aggregated metrics per student and renders the results in a JavaFX
 * {@link TableView}.</p>
 */
public class StaffAnalyticsPage {
    private final DatabaseHelper databaseHelper;
    private final String staffUsername;
    private TableView<StudentMetric> analyticsTable;

    /**
     * A simple data model that represents aggregated activity metrics for a student.
     */
    public static class StudentMetric {
        private final String userName;
        private final String name;
        private final int questionCount;
        private final int answerCount;
        private final String lastActivity;

        /**
         * Constructs a StudentMetric instance.
         *
         * @param userName the student's username
         * @param name the student's display name
         * @param questionCount the number of questions the student has posted
         * @param answerCount the number of answers the student has contributed
         * @param lastActivity ISO-like timestamp string of the student's last activity
         */
        public StudentMetric(String userName, String name, int questionCount,
                             int answerCount, String lastActivity) {
            this.userName = userName;
            this.name = name;
            this.questionCount = questionCount;
            this.answerCount = answerCount;
            this.lastActivity = lastActivity;
        }

        /**
         * Returns the student's username.
         *
         * @return the username
         */
        public String getUserName() { return userName; }

        /**
         * Returns the student's display name.
         *
         * @return the display name
         */
        public String getName() { return name; }

        /**
         * Returns the student's question count.
         *
         * @return the number of questions posted by the student
         */
        public int getQuestionCount() { return questionCount; }

        /**
         * Returns the student's answer count.
         *
         * @return the number of answers posted by the student
         */
        public int getAnswerCount() { return answerCount; }

        /**
         * Returns the timestamp (string) of the student's last activity.
         *
         * @return the last activity timestamp as a string
         */
        public String getLastActivity() { return lastActivity; }

        /**
         * JavaFX property helper for the student's username (for TableView).
         *
         * @return a StringProperty wrapping the username
         */
        public javafx.beans.property.StringProperty userNameProperty() {
            return new javafx.beans.property.SimpleStringProperty(userName);
        }

        /**
         * JavaFX property helper for the student's display name (for TableView).
         *
         * @return a StringProperty wrapping the display name
         */
        public javafx.beans.property.StringProperty nameProperty() {
            return new javafx.beans.property.SimpleStringProperty(name);
        }

        /**
         * JavaFX property helper for the question count (for TableView).
         *
         * @return an IntegerProperty wrapping the question count
         */
        public javafx.beans.property.IntegerProperty questionCountProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(questionCount);
        }

        /**
         * JavaFX property helper for the answer count (for TableView).
         *
         * @return an IntegerProperty wrapping the answer count
         */
        public javafx.beans.property.IntegerProperty answerCountProperty() {
            return new javafx.beans.property.SimpleIntegerProperty(answerCount);
        }

        /**
         * JavaFX property helper for the last activity timestamp (for TableView).
         *
         * @return a StringProperty wrapping the last activity timestamp
         */
        public javafx.beans.property.StringProperty lastActivityProperty() {
            return new javafx.beans.property.SimpleStringProperty(lastActivity);
        }
    }

    /**
     * Constructs a StaffAnalyticsPage.
     *
     * @param databaseHelper the {@link DatabaseHelper} instance used to fetch analytics data
     * @param staffUsername the username of the staff member viewing analytics
     */
    public StaffAnalyticsPage(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the analytics UI on the provided stage. The UI contains a table of
     * student metrics and a back button to return to the staff dashboard.
     *
     * @param primaryStage the application's main {@link Stage} on which the page will be shown
     */
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

    /**
     * Initialize and configure the analytics table columns and cell factories.
     * This method prepares {@link #analyticsTable} for use by {@link #refreshAnalytics()}.
     */
    private void createAnalyticsTable() {
        // Implementation for analytics table
        analyticsTable = new TableView<>();
        // Example column wiring (caller can extend)
        TableColumn<StudentMetric, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(cell -> cell.getValue().userNameProperty());
        userCol.setPrefWidth(150);

        TableColumn<StudentMetric, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());
        nameCol.setPrefWidth(200);

        TableColumn<StudentMetric, Number> qCountCol = new TableColumn<>("Questions");
        qCountCol.setCellValueFactory(cell -> cell.getValue().questionCountProperty());
        qCountCol.setPrefWidth(100);

        TableColumn<StudentMetric, Number> aCountCol = new TableColumn<>("Answers");
        aCountCol.setCellValueFactory(cell -> cell.getValue().answerCountProperty());
        aCountCol.setPrefWidth(100);

        TableColumn<StudentMetric, String> lastCol = new TableColumn<>("Last Activity");
        lastCol.setCellValueFactory(cell -> cell.getValue().lastActivityProperty());
        lastCol.setPrefWidth(200);

        analyticsTable.getColumns().addAll(userCol, nameCol, qCountCol, aCountCol, lastCol);
    }

    /**
     * Fetches analytic records from the database and updates the table.
     * Errors are presented to the user via {@link #showAlert(Alert.AlertType, String, String)}.
     */
    private void refreshAnalytics() {
        try {
            ObservableList<StudentMetric> rows = FXCollections.observableArrayList();
            ResultSet rs = databaseHelper.getStudentActivityMetrics();

            while (rs != null && rs.next()) {
                StudentMetric m = new StudentMetric(
                    rs.getString("user_name"),
                    rs.getString("display_name"),
                    rs.getInt("question_count"),
                    rs.getInt("answer_count"),
                    rs.getTimestamp("last_activity").toString()
                );
                rows.add(m);
            }
            analyticsTable.setItems(rows);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load analytics: " + e.getMessage());
        }
    }

    /**
     * Utility to show an alert dialog to the user.
     *
     * @param type the alert type (e.g., INFORMATION, WARNING, ERROR)
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
