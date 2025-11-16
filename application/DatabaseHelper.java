package databasePart1;

import java.sql.*;
import java.util.UUID;
import java.time.LocalDateTime;

import application.User;
import application.Question;
import application.Questions;
import application.Answer;
import application.Answers;
import application.Review;
import application.Reviews;
import java.util.List;
import java.util.ArrayList;
import application.Role;
import application.TrustedReviewer;

/**
 * The DatabaseHelper class provides comprehensive database management functionality
 * for the CSE360 application. It handles user authentication, question/answer management,
 * review systems, role requests, and administrative features.
 * 
 * <p>This integrated version combines features from multiple team members including
 * Josh's instructor functionality and team Q&A features with trusted reviewer weighting.</p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>User registration and authentication</li>
 *   <li>Question and answer management</li>
 *   <li>Review system for questions and answers</li>
 *   <li>Trusted reviewer management with weights</li>
 *   <li>Role request processing</li>
 *   <li>Content moderation</li>
 *   <li>Administrative request handling</li>
 * </ul>
 * 
 * @author Josh and Team
 * @version 1.0
 * @since 2024
 */
public class DatabaseHelper {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    // Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private Connection connection = null;
    private Statement statement = null; 

    /**
     * Establishes connection to the H2 database and initializes required tables.
     * This method must be called before any other database operations.
     *
     * @throws SQLException if database connection fails or table creation encounters errors
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            // statement.execute("DROP ALL OBJECTS"); // (optional) clear DB

            createTables();  // Create the necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }
    
    
    /**
     * Creates staff-specific tables if they don't exist
     */
    private void createStaffTables() throws SQLException {
        // Staff Discussion Board Table
        String staffDiscussionTable = "CREATE TABLE IF NOT EXISTS StaffDiscussions ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "staff_id VARCHAR(255), "
            + "title VARCHAR(500), "
            + "content VARCHAR(2000), "
            + "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "is_private BOOLEAN DEFAULT TRUE, "
            + "FOREIGN KEY (staff_id) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(staffDiscussionTable);

        // Staff Escalation Requests Table
        String escalationTable = "CREATE TABLE IF NOT EXISTS StaffEscalations ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "staff_id VARCHAR(255), "
            + "student_id VARCHAR(255), "
            + "issue_type VARCHAR(100), "
            + "description VARCHAR(2000), "
            + "priority VARCHAR(50) DEFAULT 'MEDIUM', "
            + "status VARCHAR(50) DEFAULT 'OPEN', "
            + "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "resolved_date TIMESTAMP, "
            + "resolved_by VARCHAR(255), "
            + "FOREIGN KEY (staff_id) REFERENCES cse360users(userName), "
            + "FOREIGN KEY (student_id) REFERENCES cse360users(userName), "
            + "FOREIGN KEY (resolved_by) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(escalationTable);

        // Content Moderation Log Table
        String moderationLogTable = "CREATE TABLE IF NOT EXISTS StaffModerationLog ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "staff_id VARCHAR(255), "
            + "content_type VARCHAR(50), "
            + "content_id INT, "
            + "action VARCHAR(100), "
            + "original_content VARCHAR(2000), "
            + "modified_content VARCHAR(2000), "
            + "moderation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (staff_id) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(moderationLogTable);
    }
    
    

    /**
     * Creates all required database tables if they don't exist.
     * Includes tables for users, questions, answers, reviews, and administrative features.
     *
     * @throws SQLException if table creation fails
     */
    private void createTables() throws SQLException {
        // User table
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "userRole VARCHAR(255), "
                + "name VARCHAR(255), "
                + "email VARCHAR(255))";
        statement.execute(userTable);
        
        // Invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(4) PRIMARY KEY, "
                + "userRole VARCHAR(200), "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "userTime TIMESTAMP )";
        statement.execute(invitationCodesTable);
        
        // Questions table - ADD created_date column
        String questionTable = "CREATE TABLE IF NOT EXISTS Questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "parent_question_id INT, "
                + "userName VARCHAR(255), "
                + "title VARCHAR(255), "
                + "text VARCHAR(500), "
                + "resolved BOOLEAN DEFAULT FALSE, "
                + "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " 
                + "FOREIGN KEY (parent_question_id) REFERENCES Questions(id) ON DELETE CASCADE"
                + ")";
        statement.execute(questionTable);
        
        // Answers table - ADD created_date column  
        String answerTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(500) NOT NULL, "
                + "userName VARCHAR(255), "
                + "resolves BOOLEAN DEFAULT FALSE, "
                + "question_id INT NOT NULL, "
                + "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " 
                + "FOREIGN KEY (question_id) REFERENCES Questions(id) ON DELETE CASCADE"
                + ")";
        statement.execute(answerTable);
        
        // === INTEGRATED: Reviews table with question_id  ===
        String reviewTable = "CREATE TABLE IF NOT EXISTS Reviews ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "text VARCHAR(500) NOT NULL, "
            + "reviewer VARCHAR(255), "
            + "question_id INT, "  
            + "answer_id INT, "    
            + "FOREIGN KEY (question_id) REFERENCES Questions(id) ON DELETE CASCADE, "
            + "FOREIGN KEY (answer_id) REFERENCES Answers(id) ON DELETE CASCADE"
            + ")";
        statement.execute(reviewTable);
        
        // === FROM TEAM VERSION: Trusted Reviewers table ===
        String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS TrustedReviewers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255), "            
                + "trustedUserName VARCHAR(255), "     
                + "FOREIGN KEY (userName) REFERENCES cse360users(userName) ON DELETE CASCADE, "
                + "FOREIGN KEY (trustedUserName) REFERENCES cse360users(userName) ON DELETE CASCADE"
                + ")";
        statement.execute(trustedReviewersTable);
        
        // === FROM JOSH'S WORK: Instructor Feature Tables ===
        
        // Role Request Table
        String roleRequestTable = "CREATE TABLE IF NOT EXISTS RoleRequests ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "student_id VARCHAR(255), "
            + "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "status VARCHAR(50) DEFAULT 'PENDING', "
            + "reviewed_by VARCHAR(255), "
            + "review_date TIMESTAMP, "
            + "FOREIGN KEY (student_id) REFERENCES cse360users(userName), "
            + "FOREIGN KEY (reviewed_by) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(roleRequestTable);
        
        // Content Moderation Table
        String moderationTable = "CREATE TABLE IF NOT EXISTS ContentModeration ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "moderator_id VARCHAR(255), "
            + "content_type VARCHAR(50), "
            + "content_id INT, "
            + "action VARCHAR(50), "
            + "reason VARCHAR(500), "
            + "moderated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (moderator_id) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(moderationTable);
        
        // Reviewer Scorecard Table
        String scorecardTable = "CREATE TABLE IF NOT EXISTS ReviewerScorecards ("
            + "reviewer_id VARCHAR(255) PRIMARY KEY, "
            + "review_count INT DEFAULT 0, "
            + "average_rating DECIMAL(3,2) DEFAULT 0.0, "
            + "helpfulness_score DECIMAL(3,2) DEFAULT 0.0, "
            + "response_time_hours DECIMAL(5,2) DEFAULT 0.0, "
            + "trust_score DECIMAL(3,2) DEFAULT 0.0, "
            + "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (reviewer_id) REFERENCES cse360users(userName)"
            + ")";
        statement.execute(scorecardTable);
        
        // Admin Request Table
        String adminRequestTable = "CREATE TABLE IF NOT EXISTS AdminRequests ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "instructor_id VARCHAR(255), "
            + "description VARCHAR(1000), "
            + "status VARCHAR(50) DEFAULT 'OPEN', "
            + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "closed_at TIMESTAMP, "
            + "closed_by VARCHAR(255), "
            + "original_request_id INT, "
            + "FOREIGN KEY (instructor_id) REFERENCES cse360users(userName), "
            + "FOREIGN KEY (closed_by) REFERENCES cse360users(userName), "
            + "FOREIGN KEY (original_request_id) REFERENCES AdminRequests(id)"
            + ")";
        statement.execute(adminRequestTable);
        
       
        
        // === FROM TEAM'S UPDATE: Add weight column to TrustedReviewers ===
        statement.execute("ALTER TABLE TrustedReviewers ADD COLUMN IF NOT EXISTS weight INT DEFAULT 3");
        
        createStaffTables();
    }

    // ==================== CORE METHODS ====================

    /**
     * Checks if the database is empty by counting users.
     * Useful for initial setup and determining if default data needs to be loaded.
     *
     * @return true if no users exist in the database, false otherwise
     * @throws SQLException if database query fails
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /**
     * Registers a new user in the system.
     * Stores user credentials, role, and personal information in the database.
     *
     * @param user the User object containing registration information
     * @throws SQLException if user insertion fails or username already exists
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, userRole, name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.executeUpdate();
        }
    }

    /**
     * Authenticates a user by verifying username and password.
     * Includes debug logging for login attempts.
     *
     * @param user the User object containing login credentials
     * @return true if credentials are valid, false otherwise
     * @throws SQLException if database query fails
     */
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String dbRole = rs.getString("userRole");
                    String userRole = user.getRole();
                    System.out.println("Login attempt - Username: " + user.getUserName());
                    System.out.println("Database role: " + dbRole);
                    System.out.println("User object role: " + userRole);
                    return true; // username/password match -> allow login
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a username already exists in the system.
     *
     * @param userName the username to check
     * @return true if username exists, false otherwise
     */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Deletes a user from the system by username.
     * Note: This will cascade delete related records due to foreign key constraints.
     *
     * @param userName the username to delete
     */
    public void deleteUser(String userName) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves the role of a specific user.
     *
     * @param userName the username to query
     * @return the user's role as a string, or null if user not found
     */
    public String getUserRole(String userName) {
        String query = "SELECT userRole FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userRole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves the display name of a user.
     *
     * @param userName the username to query
     * @return the user's display name, or null if user not found
     */
    public String getUserName(String userName) {
        String query = "SELECT name FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves the email address of a user.
     *
     * @param userName the username to query
     * @return the user's email address, or null if user not found
     */
    public String getUserEmail(String userName) {
        String query = "SELECT email FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Generates a new invitation code for user registration.
     * Codes expire 15 minutes after generation and are role-specific.
     *
     * @param userRole the role that this invitation code grants
     * @return the generated 4-character invitation code
     */
    public String generateInvitationCode(String userRole) {
        String code = UUID.randomUUID().toString().substring(0, 4);
        String insertCode = "INSERT INTO InvitationCodes (code, userRole, isUsed, userTime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCode)) {
            pstmt.setString(1, code);
            pstmt.setString(2, userRole);
            pstmt.setBoolean(3, false);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }
    
    /**
     * Validates an invitation code and returns the associated role.
     * Checks if code exists, is unused, and hasn't expired.
     *
     * @param code the invitation code to validate
     * @return the user role associated with the code, or null if invalid
     */
    public String validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE"
                + " AND userTime > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userRole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Marks an invitation code as used to prevent reuse.
     *
     * @param code the invitation code to mark as used
     */
    public void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== Q&A METHODS ====================

    /**
     * Creates a new question or clarification in the system.
     * For clarification questions, provide the parent question ID.
     *
     * @param userName the author's username
     * @param title the question title
     * @param text the question content
     * @param parentId the parent question ID for clarifications, null for main questions
     */
    public void setQuestion(String userName, String title, String text, Integer parentId) {
        String query = "INSERT INTO Questions (userName, title, text, parent_question_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, title);
            pstmt.setString(3, text);
            if (parentId != null) {
                pstmt.setInt(4, parentId);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an answer to a specific question.
     *
     * @param userName the answer author's username
     * @param text the answer content
     * @param questionId the ID of the question being answered
     */
    public void setAnswer(String userName, String text, int questionId) {
        String query = "INSERT INTO Answers (userName, text, question_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, text);
            pstmt.setInt(3, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the text of an existing answer.
     *
     * @param answerId the ID of the answer to update
     * @param newText the new answer text
     */
    public void updateAnswerText(int answerId, String newText) {
        String sql = "UPDATE Answers SET text = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newText);
            ps.setInt(2, answerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves a specific question by ID including its answers and reviews.
     *
     * @param id the question ID
     * @return the Question object with complete details, or null if not found
     */
    public Question getQuestion(int id) {
        String query = "SELECT id, userName, title, text, resolved FROM Questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Question question = new Question(
                        rs.getString("userName"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                question.setAnswers(getAnswers(question.getId()));
                question.setReviews(getQuestionReviews(question.getId())); 

                // Fetch first clarification 
                String subQuery = "SELECT id FROM Questions WHERE parent_question_id = ?";
                try (PreparedStatement subPstmt = connection.prepareStatement(subQuery)) {
                    subPstmt.setInt(1, question.getId());
                    ResultSet subRs = subPstmt.executeQuery();
                    if (subRs.next()) {
                        question.setClarification(getQuestion(subRs.getInt("id")));
                    }
                }
                return question;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Retrieves all main questions (excluding clarifications) from the database.
     *
     * @return Questions object containing all main questions
     */
    public Questions getAllQuestions() {
        Questions questions = new Questions();
        String query = "SELECT * FROM Questions WHERE parent_question_id IS NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Question question = new Question(
                        rs.getString("userName"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                question.setAnswers(getAnswers(question.getId()));
                
                String subquery = "SELECT id FROM Questions WHERE parent_question_id = ?";
                try (PreparedStatement subPstmt = connection.prepareStatement(subquery)) {
                    subPstmt.setInt(1, question.getId());
                    ResultSet subRs = subPstmt.executeQuery();
                    if (subRs.next()) {
                        question.setClarification(getQuestion(subRs.getInt("id")));
                    }
                }
                questions.addQuestion(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
    
    /**
     * Retrieves all answers for a specific question.
     *
     * @param questionId the ID of the question
     * @return Answers object containing all answers for the question
     */
    public Answers getAnswers(int questionId) {
        Answers answers = new Answers();
        String query = "SELECT id, text, userName, question_id, resolves FROM Answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Answer a = new Answer(
                        rs.getString("userName"),
                        rs.getString("text"),
                        rs.getInt("question_id"),
                        rs.getInt("id")
                );
                if (rs.getBoolean("resolves")) {
                    a.setResolves();
                }
                a.setReviews(getAnswerReviews(a.getId()));
                answers.setAnswer(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /**
     * Inserts a new question and returns its generated ID.
     *
     * @param studentName the author's username
     * @param title the question title
     * @param questionText the question content
     * @return the generated question ID, or -1 if insertion failed
     */
    public int insertQuestion(String studentName, String title, String questionText) {
        try {
            String sql = "INSERT INTO Questions (userName, title, text, parent_question_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, studentName);
                pstmt.setString(2, title);
                pstmt.setString(3, questionText);
                pstmt.setNull(4, java.sql.Types.INTEGER);
                pstmt.executeUpdate();
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error inserting question: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves all questions posted by a specific student.
     *
     * @param studentName the student's username
     * @return List of Question objects posted by the student
     */
    public List<Question> getQuestionsByStudent(String studentName) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT id, userName, title, text, resolved FROM Questions WHERE userName = ? AND parent_question_id IS NULL ORDER BY id DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Question question = new Question(
                    rs.getString("userName"),
                    rs.getString("title"), 
                    rs.getString("text"),
                    rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                questions.add(question);
            }
        } catch (SQLException e) {
            System.err.println("Error getting student questions: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Updates an existing question's title and content.
     *
     * @param questionId the ID of the question to update
     * @param newTitle the new question title
     * @param newText the new question content
     * @return true if update was successful, false otherwise
     */
    public boolean updateQuestion(int questionId, String newTitle, String newText) {
        String sql = "UPDATE Questions SET title = ?, text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newText);
            pstmt.setInt(3, questionId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks an answer as resolving or not resolving its question.
     *
     * @param answerId the ID of the answer
     * @param resolves true if the answer resolves the question, false otherwise
     */
    public void answerResolves(int answerId, boolean resolves) {
        String sql = "UPDATE Answers SET resolves = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolves);
            pstmt.setInt(2, answerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Marks a question as resolved or unresolved.
     *
     * @param questionId the ID of the question
     * @param resolved true if the question is resolved, false otherwise
     */
    public void questionResolved(int questionId, boolean resolved) {
        String sql = "UPDATE Questions SET resolved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolved);
            pstmt.setInt(2, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== REVIEW METHODS ====================

    /**
     * Adds a review to a specific question.
     *
     * @param questionId the ID of the question being reviewed
     * @param reviewer the username of the reviewer
     * @param text the review content
     */
    public void addQuestionReview(int questionId, String reviewer, String text) {
        String query = "INSERT INTO Reviews (text, reviewer, question_id, answer_id) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, text);
            pstmt.setString(2, reviewer);
            pstmt.setInt(3, questionId);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a review to a specific answer.
     *
     * @param answerId the ID of the answer being reviewed
     * @param reviewer the username of the reviewer
     * @param text the review content
     */
    public void addAnswerReview(int answerId, String reviewer, String text) {
        String query = "INSERT INTO Reviews (text, reviewer, question_id, answer_id) VALUES (?, ?, NULL, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, text);
            pstmt.setString(2, reviewer);
            pstmt.setInt(3, answerId);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all reviews submitted by a specific user.
     *
     * @param userName the reviewer's username
     * @return Reviews object containing all reviews by the user
     */
    public Reviews getReviewsByUser(String userName) {
        Reviews reviews = new Reviews();
        String query = "SELECT id, text, reviewer, question_id, answer_id FROM Reviews WHERE reviewer = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review(
                    rs.getString("text"),
                    rs.getString("reviewer"),
                    rs.getInt("id")
                );
                // Set answer_id if present (for team compatibility)
                if (rs.getInt("answer_id") != 0) {
                    review.setAnswerId(rs.getInt("answer_id"));
                }
                reviews.addReview(review);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Retrieves all reviews for a specific answer.
     *
     * @param answerId the ID of the answer
     * @return Reviews object containing all reviews for the answer
     */
    public Reviews getAnswerReviews(int answerId) {
        Reviews reviews = new Reviews();
        String query = "SELECT id, text, reviewer, answer_id FROM Reviews WHERE answer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Review review = new Review(rs.getString("text"), rs.getString("reviewer"), rs.getInt("id"));
                review.setAnswerId(answerId);
                reviews.addReview(review);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Retrieves all reviews for a specific question.
     *
     * @param questionId the ID of the question
     * @return Reviews object containing all reviews for the question
     */
    public Reviews getQuestionReviews(int questionId) {
        Reviews reviews = new Reviews();
        String query = "SELECT id, text, reviewer, question_id FROM Reviews WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Review review = new Review(rs.getString("text"), rs.getString("reviewer"), rs.getInt("question_id"));
                reviews.addReview(review);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Updates the text of an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param newText the new review text
     */
    public void updateReview(int reviewId, String newText) {
        String query = "UPDATE Reviews SET text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, reviewId);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a review from the system.
     *
     * @param reviewId the ID of the review to delete
     */
    public void deleteReview(int reviewId) {
        String query = "DELETE FROM Reviews WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reviewId);
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== TRUSTED REVIEWER METHODS ====================

    /**
     * Retrieves a complete User object by username with robust role parsing.
     *
     * @param userName the username to search for
     * @return User object with complete information, or null if not found
     */
    public User getUserByUsername(String userName) {
        String query = "SELECT * FROM cse360users WHERE userName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            if(rs.next()) {
                // Robust role parsing for multi-role strings
                Role role = Role.student;
                String roles = rs.getString("userRole");
                if (roles != null) {
                    if (roles.contains("admin")) role = Role.admin;
                    else if (roles.contains("instructor")) role = Role.instructor;
                    else if (roles.contains("staff")) role = Role.staff;
                    else if (roles.contains("reviewer")) role = Role.reviewer;
                }
                
                User user = new User(
                    rs.getString("userName"),
                    rs.getString("password"),
                    role,
                    rs.getString("name"),
                    rs.getString("email")
                );
                
                ArrayList<User> trusted = getTrustedReviewers(userName);
                user.setTrustedReviewers(trusted);
                
                return user;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a trusted reviewer relationship between two users.
     *
     * @param userName the user who is adding a trusted reviewer
     * @param trustedUser the user being trusted as a reviewer
     */
    public void addTrustedReviewer(String userName, User trustedUser) {
        String query = "INSERT INTO TrustedReviewers (userName, trustedUserName) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, trustedUser.getUserName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all trusted reviewers for a specific user.
     *
     * @param userName the user whose trusted reviewers to retrieve
     * @return ArrayList of User objects representing trusted reviewers
     */
    public ArrayList<User> getTrustedReviewers(String userName) {
        ArrayList<User> trustedUsers = new ArrayList<>();
        String query = "SELECT trustedUserName FROM TrustedReviewers WHERE userName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String trustedUserName = rs.getString("trustedUserName");
                User trustedUser = getUserByUsername(trustedUserName);
                if (trustedUser != null && trustedUser.getRole().contains("reviewer")) {
                    trustedUsers.add(trustedUser);
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return trustedUsers;
    }

    /**
     * Retrieves usernames of all trusted reviewers for a specific user, ordered by weight.
     *
     * @param userName the user whose trusted reviewers to retrieve
     * @return ArrayList of trusted reviewer usernames
     */
    public ArrayList<String> getTrustedReviewersUsername(String userName) {
        ArrayList<String> reviewers = new ArrayList<>();
        String query = "SELECT trustedUserName FROM TrustedReviewers WHERE userName = ? " +
                       "ORDER BY weight DESC, trustedUserName ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reviewers.add(rs.getString("trustedUserName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviewers;
    }

    /**
     * Updates a trusted reviewer relationship (replaces one trusted reviewer with another).
     *
     * @param userName the user who owns the trusted reviewer list
     * @param oldTrustedUser the current trusted reviewer to replace
     * @param newTrustedUser the new trusted reviewer
     */
    public void updateTrustedReviewer(String userName, User oldTrustedUser, User newTrustedUser) {
        String query = "UPDATE TrustedReviewers SET trustedUserName = ? WHERE userName = ? AND trustedUserName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newTrustedUser.getUserName());
            pstmt.setString(2, userName);
            pstmt.setString(3, oldTrustedUser.getUserName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a trusted reviewer relationship.
     *
     * @param userName the user who owns the trusted reviewer list
     * @param trustedUser the trusted reviewer to remove
     */
    public void deleteTrustedReviewer(String userName, User trustedUser) {
        String query = "DELETE FROM TrustedReviewers WHERE userName = ? AND trustedUserName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, trustedUser.getUserName());
            pstmt.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== WEIGHT-AWARE TRUSTED REVIEWER METHODS ====================

    /**
     * Inserts or updates a student's trusted reviewer with a specific weight.
     *
     * @param userName the user who owns the trusted reviewer list
     * @param trustedUserName the username of the trusted reviewer
     * @param weight the weight assigned to this trusted reviewer (1-10)
     */
    public void upsertTrustedReviewer(String userName, String trustedUserName, int weight) {
        String update = "UPDATE TrustedReviewers SET weight = ? WHERE userName = ? AND trustedUserName = ?";
        try (PreparedStatement up = connection.prepareStatement(update)) {
            up.setInt(1, weight);
            up.setString(2, userName);
            up.setString(3, trustedUserName);
            int rows = up.executeUpdate();
            if (rows == 0) {
                String insert = "INSERT INTO TrustedReviewers (userName, trustedUserName, weight) VALUES (?, ?, ?)";
                try (PreparedStatement ins = connection.prepareStatement(insert)) {
                    ins.setString(1, userName);
                    ins.setString(2, trustedUserName);
                    ins.setInt(3, weight);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns weight for (student, reviewer) or null if not trusted.
     *
     * @param userName the user who owns the trusted reviewer list
     * @param trustedUserName the username of the trusted reviewer
     * @return the weight assigned to this trusted reviewer, or null if not found
     */
    public Integer getTrustedReviewerWeight(String userName, String trustedUserName) {
        String sql = "SELECT weight FROM TrustedReviewers WHERE userName = ? AND trustedUserName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, trustedUserName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("weight");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete trusted reviewer by names (easier to call from UI).
     *
     * @param userName the user who owns the trusted reviewer list
     * @param trustedUserName the username of the trusted reviewer to remove
     */
    public void deleteTrustedReviewerByName(String userName, String trustedUserName) {
        String sql = "DELETE FROM TrustedReviewers WHERE userName = ? AND trustedUserName = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            ps.setString(2, trustedUserName);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get list with weights, ordered by weight desc then name.
     *
     * @param userName the user whose trusted reviewers to retrieve
     * @return ArrayList of TrustedReviewer objects with weights
     */
    public ArrayList<TrustedReviewer> getTrustedReviewersWithWeights(String userName) {
        ArrayList<TrustedReviewer> res = new ArrayList<>();
        String sql = "SELECT trustedUserName, weight FROM TrustedReviewers WHERE userName = ? " +
                     "ORDER BY weight DESC, trustedUserName ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(new TrustedReviewer(rs.getString("trustedUserName"), rs.getInt("weight")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    // ==================== USER MANAGEMENT METHODS ====================

    /**
     * Updates the roles assigned to a user.
     *
     * @param userName the username to update
     * @param roleString the new role string (can contain multiple comma-separated roles)
     * @return true if update was successful, false otherwise
     */
    public boolean updateUserRoles(String userName, String roleString) {
        String sql = "UPDATE cse360users SET userRole = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roleString);
            pstmt.setString(2, userName);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user roles: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Counts the number of admin users in the system.
     *
     * @return the number of users with admin role
     */
    public int countAdmins() {
        String sql = "SELECT COUNT(*) AS adminCount FROM cse360users WHERE userRole LIKE '%admin%'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("adminCount");
            }
        } catch (SQLException e) {
            System.err.println("Error counting admins: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Safely retrieves user role, returning empty string instead of null.
     *
     * @param userName the username to query
     * @return the user's role or empty string if not found
     */
    public String getUserRoleSafe(String userName) {
        String role = getUserRole(userName);
        return role == null ? "" : role;
    }

    /**
     * Retrieves all users from the system.
     *
     * @return List of all User objects in the system
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userName, password, userRole, name, email FROM cse360users ORDER BY userName";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String userRoleString = rs.getString("userRole");
                Role primaryRole = Role.student; // default role
                
                if (userRoleString != null) {
                    if (userRoleString.contains("admin")) {
                        primaryRole = Role.admin;
                    } else if (userRoleString.contains("instructor")) {
                        primaryRole = Role.instructor;
                    } else if (userRoleString.contains("staff")) {
                        primaryRole = Role.staff;
                    } else if (userRoleString.contains("reviewer")) {
                        primaryRole = Role.reviewer;
                    }
                }
                
                User user = new User(
                    rs.getString("userName"),
                    rs.getString("password"),
                    primaryRole,
                    rs.getString("name"),
                    rs.getString("email")
                );
                
                if (userRoleString != null) {
                    user.setRoles(userRoleString);
                }
                
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // ==================== INSTRUCTOR FEATURE METHODS ====================

    /**
     * Submits a request from a student to become a reviewer.
     * Prevents duplicate pending requests from the same student.
     *
     * @param studentId the username of the student requesting reviewer role
     * @return true if request was submitted successfully, false if duplicate request exists
     * @throws SQLException if database operation fails
     */
    public boolean submitReviewerRoleRequest(String studentId) throws SQLException {
        // Check if user already has a pending request
        String checkSql = "SELECT id FROM RoleRequests WHERE student_id = ? AND status = 'PENDING'";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, studentId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Already has pending request
            }
        }
        
        // Insert new request
        String sql = "INSERT INTO RoleRequests (student_id) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Processes a role request with approval or rejection decision.
     * This is a simplified version that doesn't use foreign key constraints for reviewed_by.
     *
     * @param requestId the ID of the role request to process
     * @param instructorId the username of the instructor processing the request
     * @param approved true to approve the request, false to reject
     * @throws SQLException if database operation fails
     */
    public void reviewRoleRequestSimple(int requestId, String instructorId, boolean approved) throws SQLException {
        // First update the role request status without the reviewed_by field
        String sql = "UPDATE RoleRequests SET status = ?, review_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, approved ? "APPROVED" : "REJECTED");
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
            
            // Then if approved, update the user's role
            if (approved) {
                String studentId = getStudentIdFromRoleRequest(requestId);
                if (studentId != null) {
                    addReviewerRoleToUser(studentId);
                }
            }
        }
    }

    /**
     * Retrieves all pending role requests for instructor review.
     *
     * @return ResultSet containing pending role requests with student information
     * @throws SQLException if database query fails
     */
    public ResultSet getPendingRoleRequests() throws SQLException {
        String sql = "SELECT r.*, u.name as student_name FROM RoleRequests r "
                   + "JOIN cse360users u ON r.student_id = u.userName "
                   + "WHERE r.status = 'PENDING' ORDER BY r.request_date";
        return statement.executeQuery(sql);
    }

    /**
     * Helper method to get student ID from role request.
     *
     * @param requestId the role request ID
     * @return student username associated with the request
     * @throws SQLException if database query fails
     */
    private String getStudentIdFromRoleRequest(int requestId) throws SQLException {
        String sql = "SELECT student_id FROM RoleRequests WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("student_id") : null;
        }
    }

    /**
     * Helper method to add reviewer role to a user.
     *
     * @param userName the username to grant reviewer role to
     * @throws SQLException if database update fails
     */
    private void addReviewerRoleToUser(String userName) throws SQLException {
        String currentRoles = getUserRole(userName);
        String newRoles = currentRoles == null ? "reviewer" : currentRoles + ",reviewer";
        
        String sql = "UPDATE cse360users SET userRole = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newRoles);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves the content history of a student for instructor review.
     * Includes both questions and answers posted by the student.
     *
     * @param studentId the username of the student to review
     * @return ResultSet containing the student's content history
     * @throws SQLException if database query fails
     */
    public ResultSet getStudentContentHistory(String studentId) throws SQLException {
        String sql = "SELECT 'QUESTION' as content_type, id, title, text, resolved, null as resolves "
                   + "FROM Questions WHERE userName = ? AND parent_question_id IS NULL "
                   + "UNION ALL "
                   + "SELECT 'ANSWER' as content_type, a.id, q.title, a.text, q.resolved, a.resolves "
                   + "FROM Answers a JOIN Questions q ON a.question_id = q.id "
                   + "WHERE a.userName = ? "
                   + "ORDER BY content_type";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, studentId);
        pstmt.setString(2, studentId);
        return pstmt.executeQuery();
    }

    /**
     * Records a content moderation action in the system.
     *
     * @param moderatorId the username of the moderator
     * @param contentType the type of content being moderated (QUESTION, ANSWER, REVIEW)
     * @param contentId the ID of the content being moderated
     * @param action the moderation action taken (APPROVE, REJECT, EDIT, etc.)
     * @param reason the reason for the moderation action
     * @throws SQLException if database insertion fails
     */
    public void moderateContent(String moderatorId, String contentType, int contentId, 
                              String action, String reason) throws SQLException {
        String sql = "INSERT INTO ContentModeration (moderator_id, content_type, content_id, action, reason) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, moderatorId);
            pstmt.setString(2, contentType);
            pstmt.setInt(3, contentId);
            pstmt.setString(4, action);
            pstmt.setString(5, reason);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves moderation history for a specific content item.
     *
     * @param contentType the type of content
     * @param contentId the ID of the content
     * @return ResultSet containing moderation history for the content
     * @throws SQLException if database query fails
     */
    public ResultSet getModerationHistory(String contentType, int contentId) throws SQLException {
        String sql = "SELECT * FROM ContentModeration WHERE content_type = ? AND content_id = ? ORDER BY moderated_at DESC";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, contentType);
        pstmt.setInt(2, contentId);
        return pstmt.executeQuery();
    }

    /**
     * Creates or updates a reviewer's scorecard with performance metrics.
     * Calculates trust score based on rating, helpfulness, and response time.
     *
     * @param reviewerId the username of the reviewer
     * @param reviewCount the number of reviews performed
     * @param averageRating the average rating received
     * @param helpfulnessScore the helpfulness score (0.0-1.0)
     * @param responseTime the average response time in hours
     * @throws SQLException if database operation fails
     */
    public void updateReviewerScorecard(String reviewerId, int reviewCount, double averageRating,
                                      double helpfulnessScore, double responseTime) throws SQLException {
        // Calculate trust score
        double trustScore = (averageRating * 0.4) + (helpfulnessScore * 0.3) + 
                           ((responseTime < 24 ? 1.0 : 48.0/responseTime) * 0.3);
        
        String sql = "MERGE INTO ReviewerScorecards KEY (reviewer_id) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, reviewerId);
            pstmt.setInt(2, reviewCount);
            pstmt.setDouble(3, averageRating);
            pstmt.setDouble(4, helpfulnessScore);
            pstmt.setDouble(5, responseTime);
            pstmt.setDouble(6, trustScore);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves the scorecard for a specific reviewer.
     *
     * @param reviewerId the username of the reviewer
     * @return ResultSet containing the reviewer's scorecard data
     * @throws SQLException if database query fails
     */
    public ResultSet getReviewerScorecard(String reviewerId) throws SQLException {
        String sql = "SELECT * FROM ReviewerScorecards WHERE reviewer_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, reviewerId);
        return pstmt.executeQuery();
    }

    /**
     * Retrieves all reviewer scorecards sorted by trust score.
     *
     * @return ResultSet containing all reviewer scorecards
     * @throws SQLException if database query fails
     */
    public ResultSet getAllReviewerScorecards() throws SQLException {
        String sql = "SELECT * FROM ReviewerScorecards ORDER BY trust_score DESC";
        return statement.executeQuery(sql);
    }

    /**
     * Creates a new administrative request from an instructor.
     *
     * @param instructorId the username of the instructor making the request
     * @param description the description of the administrative request
     * @return the generated request ID, or -1 if creation failed
     * @throws SQLException if database insertion fails
     */
    public int createAdminRequest(String instructorId, String description) throws SQLException {
        String sql = "INSERT INTO AdminRequests (instructor_id, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, instructorId);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            return -1;
        }
    }

    /**
     * Updates the status of an administrative request.
     *
     * @param requestId the ID of the request to update
     * @param status the new status (OPEN, CLOSED, REOPENED, etc.)
     * @param closedBy the username of the user closing the request (null if not closing)
     * @throws SQLException if database update fails
     */
    public void updateAdminRequestStatus(int requestId, String status, String closedBy) throws SQLException {
        String sql = "UPDATE AdminRequests SET status = ?, closed_by = ?, closed_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, closedBy);
            pstmt.setTimestamp(3, status.equals("CLOSED") ? new Timestamp(System.currentTimeMillis()) : null);
            pstmt.setInt(4, requestId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Reopens a closed administrative request with reference to the original.
     *
     * @param originalRequestId the ID of the original closed request
     * @param newDescription the new description for the reopened request
     * @return the ID of the new reopened request, or -1 if creation failed
     * @throws SQLException if database insertion fails
     */
    public int reopenAdminRequest(int originalRequestId, String newDescription) throws SQLException {
        String sql = "INSERT INTO AdminRequests (instructor_id, description, status, original_request_id) "
                   + "SELECT instructor_id, ?, 'REOPENED', ? FROM AdminRequests WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, newDescription);
            pstmt.setInt(2, originalRequestId);
            pstmt.setInt(3, originalRequestId);
            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            return -1;
        }
    }

    /**
     * Retrieves all administrative requests for viewing by instructors and admins.
     *
     * @return ResultSet containing all admin requests sorted by creation date
     * @throws SQLException if database query fails
     */
    public ResultSet getAllAdminRequests() throws SQLException {
        String sql = "SELECT * FROM AdminRequests ORDER BY created_at DESC";
        return statement.executeQuery(sql);
    }
    
 // ==================== STAFF ROLE METHODS ====================



    /**
     * Retrieves all questions and answers for staff monitoring
     * @return ResultSet containing all content with user information
     * @throws SQLException if database query fails
     */
    public ResultSet getAllContentForStaff() throws SQLException {
        String sql = "SELECT 'QUESTION' as content_type, q.id, q.title, q.text, "
                   + "q.userName, q.resolved, u.name as user_name "
                   + "FROM Questions q JOIN cse360users u ON q.userName = u.userName "
                   + "WHERE q.parent_question_id IS NULL "
                   + "UNION ALL "
                   + "SELECT 'ANSWER' as content_type, a.id, q.title, a.text, "
                   + "a.userName, q.resolved, u.name as user_name "
                   + "FROM Answers a JOIN Questions q ON a.question_id = q.id "
                   + "JOIN cse360users u ON a.userName = u.userName "
                   + "ORDER BY content_type DESC";
        return statement.executeQuery(sql);
    }
    /**
     * Adds a new discussion post to the staff discussion board
     * @param staffId the username of the staff member posting
     * @param title the title of the discussion post
     * @param content the content of the discussion post
     * @return true if successful, false otherwise
     * @throws SQLException if database insertion fails
     */
    public boolean addStaffDiscussion(String staffId, String title, String content) throws SQLException {
        String sql = "INSERT INTO StaffDiscussions (staff_id, title, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, staffId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all staff discussion posts
     * @return ResultSet containing all staff discussions
     * @throws SQLException if database query fails
     */
    public ResultSet getStaffDiscussions() throws SQLException {
        String sql = "SELECT sd.*, u.name as staff_name FROM StaffDiscussions sd "
                   + "JOIN cse360users u ON sd.staff_id = u.userName "
                   + "ORDER BY sd.created_date DESC";
        return statement.executeQuery(sql);
    }

    /**
     * Creates a new escalation request from staff to instructors
     * @param staffId the username of the staff member escalating
     * @param studentId the username of the student being escalated
     * @param issueType the type of issue
     * @param description detailed description of the issue
     * @param priority priority level (LOW, MEDIUM, HIGH)
     * @return the generated escalation ID, or -1 if failed
     * @throws SQLException if database insertion fails
     */
    public int createEscalationRequest(String staffId, String studentId, String issueType, 
                                     String description, String priority) throws SQLException {
        String sql = "INSERT INTO StaffEscalations (staff_id, student_id, issue_type, description, priority) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, staffId);
            pstmt.setString(2, studentId);
            pstmt.setString(3, issueType);
            pstmt.setString(4, description);
            pstmt.setString(5, priority);
            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            return -1;
        }
    }

    /**
     * Retrieves student activity metrics for staff analytics
     * @return ResultSet containing student activity data
     * @throws SQLException if database query fails
     */
    public ResultSet getStudentActivityMetrics() throws SQLException {
        String sql = "SELECT u.userName, u.name, "
                   + "COUNT(DISTINCT q.id) as question_count, "
                   + "COUNT(DISTINCT a.id) as answer_count "
                   + "FROM cse360users u "
                   + "LEFT JOIN Questions q ON u.userName = q.userName AND q.parent_question_id IS NULL "
                   + "LEFT JOIN Answers a ON u.userName = a.userName "
                   + "WHERE u.userRole LIKE '%student%' "
                   + "GROUP BY u.userName, u.name "
                   + "ORDER BY question_count DESC, answer_count DESC";
        return statement.executeQuery(sql);
    }

    /**
     * Logs a content moderation action by staff
     * @param staffId the username of the staff member
     * @param contentType type of content (QUESTION, ANSWER)
     * @param contentId the ID of the content being moderated
     * @param action the action taken (EDIT, FLAG, etc.)
     * @param originalContent the original content before modification
     * @param modifiedContent the content after modification
     * @return true if successful, false otherwise
     * @throws SQLException if database insertion fails
     */
    public boolean logContentModeration(String staffId, String contentType, int contentId,
                                      String action, String originalContent, String modifiedContent) throws SQLException {
        String sql = "INSERT INTO StaffModerationLog (staff_id, content_type, content_id, "
                   + "action, original_content, modified_content) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, staffId);
            pstmt.setString(2, contentType);
            pstmt.setInt(3, contentId);
            pstmt.setString(4, action);
            pstmt.setString(5, originalContent);
            pstmt.setString(6, modifiedContent);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all open escalation requests
     * @return ResultSet containing open escalations
     * @throws SQLException if database query fails
     */
    public ResultSet getOpenEscalations() throws SQLException {
        String sql = "SELECT se.*, s.name as staff_name, st.name as student_name "
                   + "FROM StaffEscalations se "
                   + "JOIN cse360users s ON se.staff_id = s.userName "
                   + "JOIN cse360users st ON se.student_id = st.userName "
                   + "WHERE se.status = 'OPEN' "
                   + "ORDER BY se.created_date DESC";
        return statement.executeQuery(sql);
    }

    /**
     * Updates an escalation request status
     * @param escalationId the ID of the escalation to update
     * @param status the new status
     * @param resolvedBy the username of the person resolving it
     * @return true if successful, false otherwise
     * @throws SQLException if database update fails
     */
    public boolean updateEscalationStatus(int escalationId, String status, String resolvedBy) throws SQLException {
        String sql = "UPDATE StaffEscalations SET status = ?, resolved_by = ?, "
                   + "resolved_date = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, resolvedBy);
            pstmt.setInt(3, escalationId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a user has staff role
     * @param userName the username to check
     * @return true if user has staff role, false otherwise
     */
    public boolean isStaffMember(String userName) {
        String role = getUserRole(userName);
        return role != null && role.contains("staff");
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Closes database connection and statement objects.
     * Should be called when database operations are complete to free resources.
     */
    public void closeConnection() {
        try{ 
            if(statement!=null) statement.close(); 
        } catch(SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if(connection!=null) connection.close(); 
        } catch(SQLException se){ 
            se.printStackTrace(); 
        } 
    }
}
