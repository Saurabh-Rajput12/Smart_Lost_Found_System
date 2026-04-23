package com.lostfound.dao;

import com.lostfound.models.Match;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MatchDAO.java
 * ------------------------------------------------
 * Handles database operations for the matches table.
 * Matches are written by the smart matching engine
 * and displayed to users/admin in the UI.
 * ------------------------------------------------
 */
public class MatchDAO {

    // ── 1. Save a new match pair ──────────────────────────────
    public boolean saveMatch(int lostItemId, int foundItemId) {
        // INSERT IGNORE prevents duplicate match pairs (unique constraint)
        String sql = "INSERT IGNORE INTO matches (lost_item_id, found_item_id) "
                   + "VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lostItemId);
            ps.setInt(2, foundItemId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[MatchDAO] saveMatch error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. Get all matches involving a user's items ───────────
    public List<Match> getMatchesForUser(int userId) {
        String sql = "SELECT m.*, "
                   + "li.title AS lost_item_title, li.ref_code AS lost_item_ref, "
                   + "fi.title AS found_item_title, fi.ref_code AS found_item_ref, "
                   + "li.location AS lost_location, fi.location AS found_location "
                   + "FROM matches m "
                   + "JOIN items li ON m.lost_item_id = li.item_id "
                   + "JOIN items fi ON m.found_item_id = fi.item_id "
                   + "WHERE li.reported_by = ? OR fi.reported_by = ? "
                   + "ORDER BY m.match_date DESC";

        List<Match> matches = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) matches.add(mapResultSetToMatch(rs));

        } catch (SQLException e) {
            System.err.println("[MatchDAO] getMatchesForUser error: " + e.getMessage());
        }
        return matches;
    }

    // ── 3. Get all matches (Admin view) ───────────────────────
    public List<Match> getAllMatches() {
        String sql = "SELECT m.*, "
                   + "li.title AS lost_item_title, li.ref_code AS lost_item_ref, "
                   + "fi.title AS found_item_title, fi.ref_code AS found_item_ref, "
                   + "li.location AS lost_location, fi.location AS found_location "
                   + "FROM matches m "
                   + "JOIN items li ON m.lost_item_id = li.item_id "
                   + "JOIN items fi ON m.found_item_id = fi.item_id "
                   + "ORDER BY m.match_date DESC";

        List<Match> matches = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) matches.add(mapResultSetToMatch(rs));

        } catch (SQLException e) {
            System.err.println("[MatchDAO] getAllMatches error: " + e.getMessage());
        }
        return matches;
    }

    // ── Private: map ResultSet → Match ────────────────────────
    private Match mapResultSetToMatch(ResultSet rs) throws SQLException {
        Match match = new Match();
        match.setMatchId(rs.getInt("match_id"));
        match.setLostItemId(rs.getInt("lost_item_id"));
        match.setFoundItemId(rs.getInt("found_item_id"));
        match.setLostItemTitle(rs.getString("lost_item_title"));
        match.setLostItemRefCode(rs.getString("lost_item_ref"));
        match.setFoundItemTitle(rs.getString("found_item_title"));
        match.setFoundItemRefCode(rs.getString("found_item_ref"));
        match.setLostItemLocation(rs.getString("lost_location"));
        match.setFoundItemLocation(rs.getString("found_location"));
        match.setMatchDate(rs.getTimestamp("match_date"));
        return match;
    }
}