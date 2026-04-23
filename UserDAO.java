package com.lostfound.dao;

import com.lostfound.models.User;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO.java
 * ------------------------------------------------
 * Handles all database operations for the users table.
 * Methods: register, login, getById, getAll,
 *          updateStatus, updatePassword
 * ------------------------------------------------
 */
public class UserDAO {

    // ── 1. Register a new user ────────────────────────────────
    /**
     * Inserts a new user into the database.
     * @param user User object with all fields set
     * @return true if insertion was successful
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (name, email, password, phone, role, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());  // plain text for now
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getRole());
            ps.setString(6, "ACTIVE");            // new users are active by default

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] registerUser error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. Login — find user by email + password ──────────────
    /**
     * Validates login credentials.
     * Returns the User object if credentials match, null otherwise.
     * @param email    user's email
     * @param password plain text password (hashing added in auth step)
     */
    public User loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND status = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);  // user found → return object
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] loginUser error: " + e.getMessage());
        }
        return null;  // login failed
    }

    // ── 3. Check if email already exists (for registration) ───
    /**
     * Returns true if the email is already registered.
     * Used to prevent duplicate accounts.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();  // true if any row found

        } catch (SQLException e) {
            System.err.println("[UserDAO] emailExists error: " + e.getMessage());
            return false;
        }
    }

    // ── 4. Get user by ID ─────────────────────────────────────
    /**
     * Fetches a single user by their primary key.
     * Used for session validation and profile display.
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserById error: " + e.getMessage());
        }
        return null;
    }

    // ── 5. Get all users (Admin: User Management page) ────────
    /**
     * Returns all users in the system.
     * Admin uses this to view and manage accounts.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    // ── 6. Activate or Deactivate a user (Admin only) ─────────
    /**
     * Toggles a user's status between ACTIVE and INACTIVE.
     * Inactive users cannot log in.
     * @param userId user to update
     * @param status "ACTIVE" or "INACTIVE"
     */
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUserStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── 7. Update password (Forgot Password flow) ─────────────
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword error: " + e.getMessage());
            return false;
        }
    }

    // ── 8. Get total user count (Admin dashboard) ─────────────
    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'USER'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[UserDAO] getTotalUsers error: " + e.getMessage());
        }
        return 0;
    }

    // ── Private helper: map a ResultSet row → User object ─────
    /**
     * Converts a database row into a User object.
     * Called internally by every method that reads user data.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
    
 // ── Update name and phone ──────────────────────────────────
    public boolean updateProfile(int userId, String name, String phone) {
        String sql = "UPDATE users SET name = ?, phone = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[UserDAO] updateProfile error: " + e.getMessage());
            return false;
        }
    }
}