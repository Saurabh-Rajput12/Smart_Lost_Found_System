package com.lostfound.auth;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.UserDAO;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LoginServlet.java
 * ------------------------------------------------
 * Handles POST /login
 * Validates credentials, creates session,
 * runs auto-expiry check, redirects by role.
 * ------------------------------------------------
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO         userDAO         = new UserDAO();
    private final ItemDAO         itemDAO         = new ItemDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // GET /login → just show the login page
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in, redirect to dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            redirectByRole(user, response);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/pages/login.html");
    }

    // POST /login → process credentials
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Read form fields
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        // Basic validation
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            sendError(response, "Email and password are required.");
            return;
        }

        // Attempt login
        User user = userDAO.loginUser(email.trim(), password.trim());

        if (user == null) {
            // Invalid credentials or inactive account
            sendError(response, "Invalid email or password. Please try again.");
            return;
        }

        if (!user.isActive()) {
            sendError(response, "Your account has been deactivated. Contact admin.");
            return;
        }

        // ── Login successful ──────────────────────────────────

        // 1. Run auto-expiry check on every login
        itemDAO.expireOldItems();

        // 2. Create a new session and store the user object
        HttpSession session = request.getSession(true);
        session.setAttribute("user",   user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role",   user.getRole());
        session.setAttribute("name",   user.getName());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout

        // 3. Store unread notification count in session
        int unreadCount = notificationDAO.getUnreadCount(user.getUserId());
        session.setAttribute("unreadCount", unreadCount);

        // 4. Redirect based on role
        redirectByRole(user, response);
    }

    // ── Helper: redirect ADMIN → admin dashboard, USER → user dashboard
    private void redirectByRole(User user,
                                HttpServletResponse response)
            throws IOException {
        if (user.isAdmin()) {
            response.sendRedirect("pages/admin-dashboard.html");
        } else {
            response.sendRedirect("pages/user-dashboard.html");
        }
    }

    // ── Helper: redirect back to login with error message
    private void sendError(HttpServletResponse response, String message)
            throws IOException {
        response.sendRedirect("pages/login.html?error=" +
            java.net.URLEncoder.encode(message, "UTF-8"));
    }
}