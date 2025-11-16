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
 * The {@code StaffHelpToolsPage} class provides a JavaFX interface that allows
 * staff members to access help tools intended to assist students with technical,
 * navigation, or account-related issues. This page is part of the staff workflow
 * introduced in HW4 and is accessible from the {@link StaffHomePage}.
 * <p>
 * The current implementation presents a placeholder interface with a navigation
 * option back to the staff dashboard. 
 * </ul>
 * </p>
 *
 * <p><b>Role Context:</b> This tool supports the Staff role user stories by giving 
 * staff members access to utilities that help them support students and assist 
 * instructors in maintaining the platform.</p>
 */
public class StaffHelpToolsPage {

    /** Database helper providing access to system data. */
    private final DatabaseHelper databaseHelper;

    /** Username of the staff member currently logged in. */
    private final String staffUsername;

    /**
     * Constructs a new {@code StaffHelpToolsPage}.
     *
     * @param databaseHelper the shared {@link DatabaseHelper} instance used for database operations
     * @param staffUsername the username of the staff member accessing the page
     */
    public StaffHelpToolsPage(DatabaseHelper databaseHelper, String staffUsername) {
        this.databaseHelper = databaseHelper;
        this.staffUsername = staffUsername;
    }

    /**
     * Displays the Staff Help Tools interface on the given stage.  
     * This method constructs the layout, loads UI components, and initializes 
     * navigation functionality.
     *
     * @param primaryStage the main application stage on which the UI will be displayed
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Student Help Tools");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Placeholder: Additional help tool UI elements will be added here in HW4 expansion.

        Button backBtn = new Button("Back to Dashboard");
        backBtn.setOnAction(e -> new StaffHomePage(databaseHelper, staffUsername).show(primaryStage));

        layout.getChildren().addAll(titleLabel, backBtn);

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Student Help Tools");
    }
}
