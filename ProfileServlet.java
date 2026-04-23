package com.lostfound.servlet;

import com.google.gson.Gson;
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
import java.util.HashMap;
import java.util.Map;

/**
 * ProfileServlet.java
 * ------------------------------------------------
 * GET  /profile         → returns current user's data as JSON
 * POST /profile/update  → updates name, phone, password
 * ------------------------------------------------
 */
@WebServlet(urlPatterns = {"/profile", "/profile/update"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson    gson    = new Gson();

    // GET /profile → return user data
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user = (User) session.getAttribute("user");
        User freshUser = userDAO.getUserById(user.getUserId());

        if (freshUser == null) {
            out.write("{\"success\":false,\"message\":\"User not found.\"}");
            return;
        }

        // Never send password to frontend
        Map<String, Object> data = new HashMap<>();
        data.put("userId",    freshUser.getUserId());
        data.put("name",      freshUser.getName());
        data.put("email",     freshUser.getEmail());
        data.put("phone",     freshUser.getPhone());
        data.put("role",      freshUser.getRole());
        data.put("status",    freshUser.getStatus());
        data.put("createdAt", freshUser.getCreatedAt());

        out.write(gson.toJson(data));
    }

    // POST /profile/update → update name, phone, or password
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            out.write("{\"success\":false,\"message\":\"Not logged in.\"}");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");

        String name        = request.getParameter("name");
        String phone       = request.getParameter("phone");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        // Validate name
        if (name == null || name.trim().length() < 2) {
            out.write("{\"success\":false,\"message\":\"Name must be at least 2 characters.\"}");
            return;
        }

        // Update name and phone
        boolean updated = userDAO.updateProfile(
            sessionUser.getUserId(),
            name.trim(),
            phone != null ? phone.trim() : ""
        );

        if (!updated) {
            out.write("{\"success\":false,\"message\":\"Failed to update profile.\"}");
            return;
        }

        // Update password if provided
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < 6) {
                out.write("{\"success\":false," +
                          "\"message\":\"New password must be at least 6 characters.\"}");
                return;
            }

            // Verify old password first
            User check = userDAO.loginUser(
                sessionUser.getEmail(), oldPassword
            );
            if (check == null) {
                out.write("{\"success\":false," +
                          "\"message\":\"Current password is incorrect.\"}");
                return;
            }

            userDAO.updatePassword(sessionUser.getEmail(), newPassword);
        }

        // Refresh session user object
        User refreshed = userDAO.getUserById(sessionUser.getUserId());
        if (refreshed != null) {
            session.setAttribute("user", refreshed);
            session.setAttribute("name", refreshed.getName());
        }

        out.write("{\"success\":true,\"message\":\"Profile updated successfully!\"}");
    }
}