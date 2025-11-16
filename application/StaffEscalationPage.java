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
 * The {@code StaffEscalationPage} class provides a JavaFX interface that enables
 * staff members to escalate complex student issues to instructors. This page 
 * supports Staff-role user stories by providing a place to create and view
 * escalation requests, attach context, and forward issues that require instructor attention.
 *
 * <p>Typical usage:
 * <pre>
 *   StaffEscalationPage page = new StaffEscalationPage(databaseHelper, staffUsername);
 *   page.show(primaryStage);
 * </pre>
 * </p>
 */
public class StaffEscalationPage {

    /** Shared database helper used for loading and saving escalation data. */
    private final DatabaseHelper databaseHelper;

    /** Username of the staff member currently logged in. */
    private final String staffUsername;

    /**
     * Constructs a {@code StaffEscalationPage}.
     *
     * @param databaseHelper the {@link DatabaseHelper} instance used to store and retrieve escalation data
     * @param staffUsername the username of the staff member accessing the page
     */
    public StaffEscalationPage(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the escalation request interface on the provided stage.
     * This method builds the UI, sets up navigation, and prepares the scene.
     * Additional escalation-related UI controls can be added to support HW4 staff user stories.
     *
     * @param primaryStage the application's main {@link Stage} window on which the page will be shown
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Escalation Request System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Placeholder: escalation form and management UI elements will be added here.

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> new StaffHomePage(databaseHelper, staffUsername).show(primaryStage));

        layout.getChildren().addAll(titleLabel, backBtn);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Escalation Management");
    }
}
