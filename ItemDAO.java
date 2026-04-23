package com.lostfound.dao;

import com.lostfound.models.Item;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO.java
 * ------------------------------------------------
 * Handles all database operations for the items table.
 * Includes: report, search, filter, expiry, stats
 * ------------------------------------------------
 */
public class ItemDAO {

    // ── 1. Report a new Lost or Found item ────────────────────
    /**
     * Inserts a new item report into the database.
     * ref_code is auto-generated using generateRefCode().
     * @return true if successful
     */
    public boolean reportItem(Item item) {
        String sql = "INSERT INTO items "
                   + "(ref_code, title, description, category_id, type, status, "
                   + " location, date_occurred, image_path, reported_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Generate unique ref code: LF-2026-00001
            String refCode = generateRefCode(conn);

            ps.setString(1, refCode);
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setInt(4,    item.getCategoryId());
            ps.setString(5, item.getType());
            ps.setString(6, item.getType());   // initial status = type (LOST or FOUND)
            ps.setString(7, item.getLocation());
            ps.setObject(8, item.getDateOccurred()); // LocalDateTime works with MySQL 8
            ps.setString(9, item.getImagePath());
            ps.setInt(10,   item.getReportedBy());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ItemDAO] reportItem error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. Get item by ID (with category + reporter name) ─────
    public Item getItemById(int itemId) {
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name "
                   + "FROM items i "
                   + "JOIN categories c ON i.category_id = c.category_id "
                   + "JOIN users u ON i.reported_by = u.user_id "
                   + "WHERE i.item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToItem(rs);

        } catch (SQLException e) {
            System.err.println("[ItemDAO] getItemById error: " + e.getMessage());
        }
        return null;
    }

    // ── 3. Get all items reported by a specific user ──────────
    public List<Item> getItemsByUser(int userId) {
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name "
                   + "FROM items i "
                   + "JOIN categories c ON i.category_id = c.category_id "
                   + "JOIN users u ON i.reported_by = u.user_id "
                   + "WHERE i.reported_by = ? "
                   + "ORDER BY i.created_at DESC";

        return fetchItems(sql, userId);
    }

    // ── 4. Get ALL items (Admin dashboard) ────────────────────
    public List<Item> getAllItems() {
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name "
                   + "FROM items i "
                   + "JOIN categories c ON i.category_id = c.category_id "
                   + "JOIN users u ON i.reported_by = u.user_id "
                   + "ORDER BY i.created_at DESC";

        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) items.add(mapResultSetToItem(rs));

        } catch (SQLException e) {
            System.err.println("[ItemDAO] getAllItems error: " + e.getMessage());
        }
        return items;
    }

    // ── 5. Search + Filter items ──────────────────────────────
    /**
     * Flexible search with optional filters.
     * Any parameter can be null/empty to skip that filter.
     *
     * @param keyword    searches title and description
     * @param categoryId 0 = all categories
     * @param type       "LOST", "FOUND", or null for both
     * @param status     "LOST","FOUND","CLAIMED","EXPIRED", or null
     * @param location   partial match on location field
     * @param dateFrom   start of date range (can be null)
     * @param dateTo     end of date range (can be null)
     */
    public List<Item> searchItems(String keyword, int categoryId,
                                   String type, String status,
                                   String location,
                                   String dateFrom, String dateTo) {

        // Build query dynamically based on which filters are provided
        StringBuilder sql = new StringBuilder(
            "SELECT i.*, c.category_name, u.name AS reporter_name "
          + "FROM items i "
          + "JOIN categories c ON i.category_id = c.category_id "
          + "JOIN users u ON i.reported_by = u.user_id "
          + "WHERE 1=1 "  // always true — lets us append AND clauses safely
        );

        List<Object> params = new ArrayList<>();

        // Keyword filter: searches title and description
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (i.title LIKE ? OR i.description LIKE ?) ");
            params.add("%" + keyword.trim() + "%");
            params.add("%" + keyword.trim() + "%");
        }

        // Category filter
        if (categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }

        // Type filter (LOST or FOUND)
        if (type != null && !type.isEmpty()) {
            sql.append("AND i.type = ? ");
            params.add(type);
        }

        // Status filter
        if (status != null && !status.isEmpty()) {
            sql.append("AND i.status = ? ");
            params.add(status);
        }

        // Location filter (partial match)
        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND i.location LIKE ? ");
            params.add("%" + location.trim() + "%");
        }

        // Date range filter
        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append("AND i.date_occurred >= ? ");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append("AND i.date_occurred <= ? ");
            params.add(dateTo + " 23:59:59");
        }

        sql.append("ORDER BY i.created_at DESC");

        List<Item> items = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Set all dynamic parameters in order
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapResultSetToItem(rs));

        } catch (SQLException e) {
            System.err.println("[ItemDAO] searchItems error: " + e.getMessage());
        }

        return items;
    }

    // ── 6. Update item status ─────────────────────────────────
    /**
     * Changes an item's status.
     * Called when: claim approved → "CLAIMED", expiry → "EXPIRED"
     */
    public boolean updateItemStatus(int itemId, String status) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ItemDAO] updateItemStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── 7. Auto-expiry: mark items older than 30 days ─────────
    /**
     * Scans all active items and marks those older than 30 days
     * as EXPIRED. Called on every user login.
     * @return number of items expired
     */
    public int expireOldItems() {
        String sql = "UPDATE items "
                   + "SET status = 'EXPIRED' "
                   + "WHERE status IN ('LOST', 'FOUND') "
                   + "AND created_at < NOW() - INTERVAL 30 DAY";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = ps.executeUpdate();
            if (count > 0) {
                System.out.println("[ItemDAO] Auto-expired " + count + " item(s).");
            }
            return count;

        } catch (SQLException e) {
            System.err.println("[ItemDAO] expireOldItems error: " + e.getMessage());
            return 0;
        }
    }

    // ── 8. Dashboard statistics ───────────────────────────────
    /** Returns count of items by status for admin dashboard */
    public int getItemCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM items WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ItemDAO] getItemCountByStatus error: " + e.getMessage());
        }
        return 0;
    }

    /** Returns count of items by type (LOST or FOUND) */
    public int getItemCountByType(String type) {
        String sql = "SELECT COUNT(*) FROM items WHERE type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ItemDAO] getItemCountByType error: " + e.getMessage());
        }
        return 0;
    }

    // ── 9. Smart Matching ─────────────────────────────────────
    /**
     * Finds potential matches for a given item.
     * Matches items of the OPPOSITE type in the SAME category
     * where the title or description contains similar keywords.
     *
     * Logic:
     *   If input item is LOST  → search FOUND items
     *   If input item is FOUND → search LOST items
     *   Filter by: same category + keyword overlap in title
     */
    public List<Item> findMatches(Item item) {
        // Opposite type for matching
        String oppositeType = item.isLost() ? "FOUND" : "LOST";

        // Use first word of title as keyword (simple but effective)
        String keyword = item.getTitle().split(" ")[0];

        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name "
                   + "FROM items i "
                   + "JOIN categories c ON i.category_id = c.category_id "
                   + "JOIN users u ON i.reported_by = u.user_id "
                   + "WHERE i.type = ? "
                   + "AND i.category_id = ? "
                   + "AND i.status NOT IN ('CLAIMED', 'EXPIRED') "
                   + "AND (i.title LIKE ? OR i.description LIKE ?) "
                   + "AND i.item_id != ? "   // don't match with itself
                   + "ORDER BY i.date_occurred DESC";

        List<Item> matches = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, oppositeType);
            ps.setInt(2,    item.getCategoryId());
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            ps.setInt(5,    item.getItemId());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) matches.add(mapResultSetToItem(rs));

        } catch (SQLException e) {
            System.err.println("[ItemDAO] findMatches error: " + e.getMessage());
        }

        return matches;
    }

    // ── 10. Get recent items (Activity feed) ──────────────────
    public List<Item> getRecentItems(int limit) {
        String sql = "SELECT i.*, c.category_name, u.name AS reporter_name "
                   + "FROM items i "
                   + "JOIN categories c ON i.category_id = c.category_id "
                   + "JOIN users u ON i.reported_by = u.user_id "
                   + "ORDER BY i.created_at DESC LIMIT ?";

        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapResultSetToItem(rs));

        } catch (SQLException e) {
            System.err.println("[ItemDAO] getRecentItems error: " + e.getMessage());
        }
        return items;
    }

    // ── Private: Generate ref code LF-YYYY-XXXXX ─────────────
    /**
     * Generates the next unique reference code.
     * Format: LF-2026-00001
     * Finds the highest existing number for the current year
     * and increments it by 1.
     */
    private String generateRefCode(Connection conn) throws SQLException {
        int year = LocalDateTime.now().getYear();

        // Find the maximum sequence number for this year
        String sql = "SELECT MAX(CAST(SUBSTRING(ref_code, 9) AS UNSIGNED)) "
                   + "FROM items WHERE ref_code LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "LF-" + year + "-%");
            ResultSet rs = ps.executeQuery();

            int nextNum = 1; // default if no items exist yet
            if (rs.next() && rs.getString(1) != null) {
                nextNum = rs.getInt(1) + 1;
            }

            // Format as LF-2026-00001 (5-digit zero-padded number)
            return String.format("LF-%d-%05d", year, nextNum);
        }
    }

    // ── Private: Map ResultSet row → Item object ──────────────
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setRefCode(rs.getString("ref_code"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setType(rs.getString("type"));
        item.setStatus(rs.getString("status"));
        item.setLocation(rs.getString("location"));
        item.setReportedBy(rs.getInt("reported_by"));
        item.setReporterName(rs.getString("reporter_name"));
        item.setImagePath(rs.getString("image_path"));
        item.setCreatedAt(rs.getTimestamp("created_at"));

        // Convert SQL Timestamp → LocalDateTime
        Timestamp ts = rs.getTimestamp("date_occurred");
        if (ts != null) item.setDateOccurred(ts.toLocalDateTime());

        return item;
    }

    // ── Private: fetch helper for int parameter queries ───────
    private List<Item> fetchItems(String sql, int param) {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) items.add(mapResultSetToItem(rs));

        } catch (SQLException e) {
            System.err.println("[ItemDAO] fetchItems error: " + e.getMessage());
        }
        return items;
    }
}