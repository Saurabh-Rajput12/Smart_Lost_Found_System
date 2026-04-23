package com.lostfound.dao;

import com.lostfound.models.Notification;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO.java
 * ------------------------------------------------
 * Handles all database operations for notifications.
 * Notifications are created by the system when:
 *   - A claim is approved or rejected
 *   - A smart match is found
 * ------------------------------------------------
 */
public class NotificationDAO {

    // ── 1. Create a new notification ─────────────────────────
    public boolean createNotification(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message, is_read) "
                   + "VALUES (?, ?, 0)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    userId);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] createNotification error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. Get all notifications for a user ───────────────────
    public List<Notification> getNotificationsByUser(int userId) {
        String sql = "SELECT * FROM notifications "
                   + "WHERE user_id = ? "
                   + "ORDER BY created_at DESC";

        List<Notification> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToNotification(rs));

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] getNotificationsByUser error: " + e.getMessage());
        }
        return list;
    }

    // ── 3. Count unread notifications (for badge on login) ────
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications "
                   + "WHERE user_id = ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] getUnreadCount error: " + e.getMessage());
        }
        return 0;
    }

    // ── 4. Mark all notifications as read ─────────────────────
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 "
                   + "WHERE user_id = ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markAllAsRead error: " + e.getMessage());
            return false;
        }
    }

    // ── 5. Mark single notification as read ───────────────────
    public boolean markAsRead(int notifId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notif_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notifId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markAsRead error: " + e.getMessage());
            return false;
        }
    }

    // ── Private: map ResultSet → Notification ─────────────────
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotifId(rs.getInt("notif_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}