package com.lostfound.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet.java
 * ------------------------------------------------
 * Handles GET /logout
 * Destroys the session and redirects to login.
 * ------------------------------------------------
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Get existing session (don't create a new one)
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // destroy all session data
        }

        // Redirect to login page with logout message
        response.sendRedirect(request.getContextPath() +
            "/pages/login.html?success=You have been logged out successfully.");
    }
    
}