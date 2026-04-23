package com.lostfound.auth;

import com.lostfound.dao.UserDAO;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * RegisterServlet.java
 * ------------------------------------------------
 * Handles POST /register
 * Validates input, checks duplicate email,
 * saves new user, redirects to login.
 * ------------------------------------------------
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ── Read all form fields ──────────────────────────────
        String name     = request.getParameter("name");
        String email    = request.getParameter("email");
        String password = request.getParameter("password");
        String confirm  = request.getParameter("confirmPassword");
        String phone    = request.getParameter("phone");

        // ── Server-side validation ────────────────────────────

        // Check required fields
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            sendError(response, "All fields are required.");
            return;
        }

        // Check password match
        if (!password.equals(confirm)) {
            sendError(response, "Passwords do not match.");
            return;
        }

        // Check minimum password length
        if (password.length() < 6) {
            sendError(response, "Password must be at least 6 characters.");
            return;
        }

        // Check email format (basic)
        if (!email.contains("@") || !email.contains(".")) {
            sendError(response, "Please enter a valid email address.");
            return;
        }

        // Check if email already registered
        if (userDAO.emailExists(email.trim())) {
            sendError(response, "This email is already registered. Please login.");
            return;
        }

        // ── Build and save new user ───────────────────────────
        User newUser = new User();
        newUser.setName(name.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPassword(password);   // plain text for now
        newUser.setPhone(phone != null ? phone.trim() : "");
        newUser.setRole("USER");
        newUser.setStatus("ACTIVE");

        boolean saved = userDAO.registerUser(newUser);

        if (saved) {
            // Registration successful → redirect to login with success message
            response.sendRedirect("pages/login.html?success=Account created! Please login.");
        } else {
            sendError(response, "Registration failed. Please try again.");
        }
    }

    // ── Helper: redirect back to register with error
    private void sendError(HttpServletResponse response, String message)
            throws IOException {
        response.sendRedirect("pages/register.html?error=" +
            java.net.URLEncoder.encode(message, "UTF-8"));
    }
}