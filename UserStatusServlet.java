package com.lostfound.servlet;

import com.lostfound.dao.UserDAO;
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
 * UserStatusServlet.java
 * ------------------------------------------------
 * POST /user/status
 * Admin-only: activate or deactivate a user account.
 * ------------------------------------------------
 */
@WebServlet("/user/status")
public class UserStatusServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest  request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // ── Admin check ───────────────────────────────────
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

        // ── Read and validate parameters ──────────────────
        String userIdStr = request.getParameter("userId");
        String status    = request.getParameter("status");

        if (userIdStr == null || status == null) {
            out.write("{\"success\":false,\"message\":\"Missing parameters.\"}");
            return;
        }

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            out.write("{\"success\":false,\"message\":\"Invalid status.\"}");
            return;
        }

        int targetUserId;
        try {
            targetUserId = Integer.parseInt(userIdStr.trim());
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"Invalid user ID.\"}");
            return;
        }

        // Prevent admin from deactivating themselves
        if (targetUserId == admin.getUserId()) {
            out.write("{\"success\":false," +
                      "\"message\":\"You cannot change your own status.\"}");
            return;
        }

        boolean updated = userDAO.updateUserStatus(targetUserId, status);
        out.write(updated
            ? "{\"success\":true}"
            : "{\"success\":false,\"message\":\"Failed to update status.\"}");
    }
}