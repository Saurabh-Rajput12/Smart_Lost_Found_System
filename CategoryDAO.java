package com.lostfound.dao;

import com.lostfound.models.Category;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO.java
 * ------------------------------------------------
 * Handles all database operations for categories.
 * Mainly used to populate dropdowns in the UI.
 * ------------------------------------------------
 */
public class CategoryDAO {

    // ── 1. Get all categories (for dropdown menus) ────────────
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                ));
            }

        } catch (SQLException e) {
            System.err.println("[CategoryDAO] getAllCategories error: " + e.getMessage());
        }
        return list;
    }

    // ── 2. Get single category by ID ──────────────────────────
    public Category getCategoryById(int id) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
            }

        } catch (SQLException e) {
            System.err.println("[CategoryDAO] getCategoryById error: " + e.getMessage());
        }
        return null;
    }

    // ── 3. Add new category (Admin only) ──────────────────────
    public boolean addCategory(String categoryName) {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryName);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[CategoryDAO] addCategory error: " + e.getMessage());
            return false;
        }
    }
}