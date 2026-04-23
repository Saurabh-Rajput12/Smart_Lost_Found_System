package com.lostfound.dao;

import com.lostfound.models.Claim;
import com.lostfound.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClaimDAO.java
 * ------------------------------------------------
 * Handles all database operations for claims.
 * Users submit claims; Admin approves or rejects.
 * ------------------------------------------------
 */
public class ClaimDAO {

    // ── 1. Submit a new claim ─────────────────────────────────
    public boolean submitClaim(Claim claim) {
        String sql = "INSERT INTO claims (item_id, claimed_by, proof_description, status) "
                   + "VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    claim.getItemId());
            ps.setInt(2,    claim.getClaimedBy());
            ps.setString(3, claim.getProofDescription());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] submitClaim error: " + e.getMessage());
            return false;
        }
    }

    // ── 2. Check if user already claimed this item ────────────
    public boolean alreadyClaimed(int itemId, int userId) {
        String sql = "SELECT claim_id FROM claims WHERE item_id = ? AND claimed_by = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] alreadyClaimed error: " + e.getMessage());
            return false;
        }
    }

    // ── 3. Get all PENDING claims (Admin dashboard) ───────────
    public List<Claim> getPendingClaims() {
        String sql = "SELECT cl.*, i.title AS item_title, i.ref_code AS item_ref_code, "
                   + "u.name AS claimed_by_name "
                   + "FROM claims cl "
                   + "JOIN items i ON cl.item_id = i.item_id "
                   + "JOIN users u ON cl.claimed_by = u.user_id "
                   + "WHERE cl.status = 'PENDING' "
                   + "ORDER BY cl.claimed_at DESC";

        return fetchClaims(sql);
    }

    // ── 4. Get all claims by a specific user ──────────────────
    public List<Claim> getClaimsByUser(int userId) {
        String sql = "SELECT cl.*, i.title AS item_title, i.ref_code AS item_ref_code, "
                   + "u.name AS claimed_by_name "
                   + "FROM claims cl "
                   + "JOIN items i ON cl.item_id = i.item_id "
                   + "JOIN users u ON cl.claimed_by = u.user_id "
                   + "WHERE cl.claimed_by = ? "
                   + "ORDER BY cl.claimed_at DESC";

        List<Claim> claims = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) claims.add(mapResultSetToClaim(rs));

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] getClaimsByUser error: " + e.getMessage());
        }
        return claims;
    }

    // ── 5. Get all claims for an item ─────────────────────────
    public List<Claim> getClaimsByItem(int itemId) {
        String sql = "SELECT cl.*, i.title AS item_title, i.ref_code AS item_ref_code, "
                   + "u.name AS claimed_by_name "
                   + "FROM claims cl "
                   + "JOIN items i ON cl.item_id = i.item_id "
                   + "JOIN users u ON cl.claimed_by = u.user_id "
                   + "WHERE cl.item_id = ? "
                   + "ORDER BY cl.claimed_at DESC";

        List<Claim> claims = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) claims.add(mapResultSetToClaim(rs));

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] getClaimsByItem error: " + e.getMessage());
        }
        return claims;
    }

    // ── 6. Update claim status (Admin: Approve or Reject) ─────
    /**
     * Admin approves or rejects a claim.
     * When approved, ItemDAO.updateItemStatus() must also
     * be called to set the item to 'CLAIMED'.
     */
    public boolean updateClaimStatus(int claimId, String status) {
        String sql = "UPDATE claims SET status = ? WHERE claim_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2,    claimId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] updateClaimStatus error: " + e.getMessage());
            return false;
        }
    }

    // ── 7. Get claim by ID ────────────────────────────────────
    public Claim getClaimById(int claimId) {
        String sql = "SELECT cl.*, i.title AS item_title, i.ref_code AS item_ref_code, "
                   + "u.name AS claimed_by_name "
                   + "FROM claims cl "
                   + "JOIN items i ON cl.item_id = i.item_id "
                   + "JOIN users u ON cl.claimed_by = u.user_id "
                   + "WHERE cl.claim_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, claimId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToClaim(rs);

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] getClaimById error: " + e.getMessage());
        }
        return null;
    }

    // ── 8. Count pending claims (Admin dashboard badge) ───────
    public int getPendingClaimCount() {
        String sql = "SELECT COUNT(*) FROM claims WHERE status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] getPendingClaimCount error: " + e.getMessage());
        }
        return 0;
    }

    // ── Private helpers ───────────────────────────────────────
    private List<Claim> fetchClaims(String sql) {
        List<Claim> claims = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) claims.add(mapResultSetToClaim(rs));

        } catch (SQLException e) {
            System.err.println("[ClaimDAO] fetchClaims error: " + e.getMessage());
        }
        return claims;
    }

    private Claim mapResultSetToClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getInt("claim_id"));
        claim.setItemId(rs.getInt("item_id"));
        claim.setItemTitle(rs.getString("item_title"));
        claim.setItemRefCode(rs.getString("item_ref_code"));
        claim.setClaimedBy(rs.getInt("claimed_by"));
        claim.setClaimedByName(rs.getString("claimed_by_name"));
        claim.setProofDescription(rs.getString("proof_description"));
        claim.setStatus(rs.getString("status"));
        claim.setClaimedAt(rs.getTimestamp("claimed_at"));
        return claim;
    }
}