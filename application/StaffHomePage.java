package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * StaffHomePage - Main dashboard for staff members
 * Provides access to all staff functionalities including content monitoring,
 * discussion board, analytics, and escalation systems.
 */
public class StaffHomePage {
    private final DatabaseHelper databaseHelper;
    private final String username;

    /**
     * Constructor for call sites that only provide DatabaseHelper
     * @param databaseHelper the database helper for data operations
     */
    public StaffHomePage(DatabaseHelper databaseHelper) {
        this(databaseHelper, "Staff"); // Default username
    }

    /**
     * Constructs a StaffHomePage with database access and user context
     * @param databaseHelper the database helper for data operations
     * @param username the username of the staff member
     */
    public StaffHomePage(DatabaseHelper databaseHelper, String username) {
        this.databaseHelper = databaseHelper;
        this.username = username;
    }

    /**
     * Displays the main staff dashboard with navigation options
     * @param primaryStage the primary application stage
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30; -fx-background-color: #f8f9fa;");
        
        Label welcomeLabel = new Label("Staff Dashboard - Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label instructionLabel = new Label("Select a staff functionality:");
        instructionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Staff functionality buttons
        Button contentDashboardBtn = createStaffButton("Content Monitoring Dashboard");
        Button discussionBoardBtn = createStaffButton("Staff Discussion Board");
        Button analyticsBtn = createStaffButton("Student Analytics");
        Button escalationBtn = createStaffButton("Escalation Requests");
        Button helpToolsBtn = createStaffButton("Student Help Tools");
        Button logoutBtn = createStaffButton("Logout");

        // Button actions
        contentDashboardBtn.setOnAction(e -> {
            StaffContentDashboard contentDashboard = new StaffContentDashboard(databaseHelper, username);
            contentDashboard.show(primaryStage);
        });

        discussionBoardBtn.setOnAction(e -> {
            StaffDiscussionBoard discussionBoard = new StaffDiscussionBoard(databaseHelper, username);
            discussionBoard.show(primaryStage);
        });

        analyticsBtn.setOnAction(e -> {
            StaffAnalyticsPage analyticsPage = new StaffAnalyticsPage(databaseHelper, username);
            analyticsPage.show(primaryStage);
        });

        escalationBtn.setOnAction(e -> {
            StaffEscalationPage escalationPage = new StaffEscalationPage(databaseHelper, username);
            escalationPage.show(primaryStage);
        });

        helpToolsBtn.setOnAction(e -> {
            StaffHelpToolsPage helpToolsPage = new StaffHelpToolsPage(databaseHelper, username);
            helpToolsPage.show(primaryStage);
        });

        logoutBtn.setOnAction(e -> {
            // Go back to login selection
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        layout.getChildren().addAll(
            welcomeLabel,
            instructionLabel,
            contentDashboardBtn,
            discussionBoardBtn,
            analyticsBtn,
            escalationBtn,
            helpToolsBtn,
            logoutBtn
        );

        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Dashboard");
    }

    /**
     * Creates a consistently styled staff button
     * @param text the button text
     * @return styled Button object
     */
    private Button createStaffButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-font-size: 14px; -fx-padding: 12 24; -fx-pref-width: 300px; "
                      + "-fx-background-color: #3498db; -fx-text-fill: white; "
                      + "-fx-border-radius: 5; -fx-background-radius: 5;");
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-font-size: 14px; -fx-padding: 12 24; -fx-pref-width: 300px; "
                          + "-fx-background-color: #2980b9; -fx-text-fill: white; "
                          + "-fx-border-radius: 5; -fx-background-radius: 5;")
        );
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-font-size: 14px; -fx-padding: 12 24; -fx-pref-width: 300px; "
                          + "-fx-background-color: #3498db; -fx-text-fill: white; "
                          + "-fx-border-radius: 5; -fx-background-radius: 5;")
        );
        return button;
    }
}
