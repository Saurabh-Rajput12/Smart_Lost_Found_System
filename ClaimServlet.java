package com.lostfound.servlet;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.models.Claim;
import com.lostfound.models.Item;
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
 * ClaimServlet.java
 * ------------------------------------------------
 * POST /claim/submit
 * Allows a logged-in user to submit a claim on
 * a found item with a proof description.
 *
 * Validations:
 *  - Item must exist and be in FOUND status
 *  - User cannot claim their own item
 *  - User cannot submit duplicate claims
 * ------------------------------------------------
 */
@WebServlet("/claim/submit")
public class ClaimServlet extends HttpServlet {

    private final ClaimDAO claimDAO = new ClaimDAO();
    private final ItemDAO  itemDAO  = new ItemDAO();

    @Override
    protected void doPost(HttpServletRequest  request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ── Session check ─────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.write("{\"success\":false,\"message\":\"Not logged in.\"}");
            return;
        }

        User user   = (User) session.getAttribute("user");
        int  userId = user.getUserId();

        // ── Read parameters ───────────────────────────────
        String itemIdStr = request.getParameter("itemId");
        String proof     = request.getParameter("proofDescription");

        // ── Validate input ────────────────────────────────
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            out.write("{\"success\":false,\"message\":\"Item ID is required.\"}");
            return;
        }

        if (proof == null || proof.trim().isEmpty()) {
            out.write("{\"success\":false," +
                      "\"message\":\"Please provide proof description.\"}");
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(itemIdStr.trim());
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"Invalid item ID.\"}");
            return;
        }

        // ── Fetch the item being claimed ──────────────────
        Item item = itemDAO.getItemById(itemId);
        if (item == null) {
            out.write("{\"success\":false,\"message\":\"Item not found.\"}");
            return;
        }

        // Cannot claim your own reported item
        if (item.getReportedBy() == userId) {
            out.write("{\"success\":false," +
                      "\"message\":\"You cannot claim your own reported item.\"}");
            return;
        }

        // Can only claim items with FOUND status
        if (!"FOUND".equals(item.getStatus())) {
            out.write("{\"success\":false," +
                      "\"message\":\"This item is not available for claiming.\"}");
            return;
        }

        // Check for duplicate claim
        if (claimDAO.alreadyClaimed(itemId, userId)) {
            out.write("{\"success\":false," +
                      "\"message\":\"You have already submitted a claim for this item.\"}");
            return;
        }

        // ── Submit the claim ──────────────────────────────
        Claim claim = new Claim(itemId, userId, proof.trim());
        boolean saved = claimDAO.submitClaim(claim);

        if (saved) {
            out.write("{\"success\":true," +
                      "\"message\":\"Claim submitted! Admin will review it shortly.\"}");
        } else {
            out.write("{\"success\":false," +
                      "\"message\":\"Failed to submit claim. Please try again.\"}");
        }
    }
}