package com.lostfound.servlet;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.models.Claim;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * ClaimActionServlet.java
 * ------------------------------------------------
 * POST /claim/action
 * Admin-only endpoint to APPROVE or REJECT a claim.
 *
 * On APPROVE:
 *   1. Update claim status → APPROVED
 *   2. Update item status  → CLAIMED
 *   3. Send notification   → claim owner
 *   4. Reject all other    → pending claims on same item
 *
 * On REJECT:
 *   1. Update claim status → REJECTED
 *   2. Send notification   → claim owner
 * ------------------------------------------------
 */
@WebServlet("/claim/action")
public class ClaimActionServlet extends HttpServlet {

    private final ClaimDAO        claimDAO        = new ClaimDAO();
    private final ItemDAO         itemDAO         = new ItemDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doPost(HttpServletRequest  request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ── Admin-only check ──────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.write("{\"success\":false,\"message\":\"Not logged in.\"}");
            return;
        }

        User admin = (User) session.getAttribute("user");
        if (!admin.isAdmin()) {
            out.write("{\"success\":false," +
                      "\"message\":\"Admin access required.\"}");
            return;
        }

        // ── Read parameters ───────────────────────────────
        String claimIdStr = request.getParameter("claimId");
        String itemIdStr  = request.getParameter("itemId");
        String action     = request.getParameter("action"); // APPROVED or REJECTED

        // ── Validate ──────────────────────────────────────
        if (claimIdStr == null || itemIdStr == null || action == null) {
            out.write("{\"success\":false,\"message\":\"Missing parameters.\"}");
            return;
        }

        if (!action.equals("APPROVED") && !action.equals("REJECTED")) {
            out.write("{\"success\":false,\"message\":\"Invalid action.\"}");
            return;
        }

        int claimId, itemId;
        try {
            claimId = Integer.parseInt(claimIdStr.trim());
            itemId  = Integer.parseInt(itemIdStr.trim());
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"Invalid IDs.\"}");
            return;
        }

        // ── Fetch the claim ───────────────────────────────
        Claim claim = claimDAO.getClaimById(claimId);
        if (claim == null) {
            out.write("{\"success\":false,\"message\":\"Claim not found.\"}");
            return;
        }

        // ── Process action ────────────────────────────────
        boolean updated = claimDAO.updateClaimStatus(claimId, action);

        if (!updated) {
            out.write("{\"success\":false," +
                      "\"message\":\"Failed to update claim status.\"}");
            return;
        }

        if ("APPROVED".equals(action)) {

            // 1. Mark item as CLAIMED
            itemDAO.updateItemStatus(itemId, "CLAIMED");

            // 2. Notify the approved claimant
            String approveMsg = "✅ Your claim for item '"
                              + claim.getItemTitle()
                              + "' (" + claim.getItemRefCode() + ")"
                              + " has been APPROVED. "
                              + "Please collect your item from admin.";
            notificationDAO.createNotification(
                claim.getClaimedBy(), approveMsg
            );

            // 3. Reject and notify all OTHER pending claims on same item
            claimDAO.getClaimsByItem(itemId).forEach(otherClaim -> {
                if (otherClaim.getClaimId() != claimId
                    && "PENDING".equals(otherClaim.getStatus())) {

                    claimDAO.updateClaimStatus(
                        otherClaim.getClaimId(), "REJECTED"
                    );

                    String rejectMsg = "❌ Your claim for item '"
                                     + claim.getItemTitle()
                                     + "' has been rejected because "
                                     + "another claim was approved.";
                    notificationDAO.createNotification(
                        otherClaim.getClaimedBy(), rejectMsg
                    );
                }
            });

        } else {
            // REJECTED — just notify the claimant
            String rejectMsg = "❌ Your claim for item '"
                             + claim.getItemTitle()
                             + "' (" + claim.getItemRefCode() + ")"
                             + " has been REJECTED by admin. "
                             + "Please contact admin for details.";
            notificationDAO.createNotification(
                claim.getClaimedBy(), rejectMsg
            );
        }

        out.write("{\"success\":true,\"message\":\"Claim "
                + action.toLowerCase() + " successfully.\"}");
    }
}