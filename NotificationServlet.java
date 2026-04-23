package com.lostfound.servlet;

import com.lostfound.dao.NotificationDAO;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * NotificationServlet.java
 * ------------------------------------------------
 * POST /notifications/read
 * Marks all notifications as read for the
 * currently logged-in user.
 * ------------------------------------------------
 */
@WebServlet("/notifications/read")
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doPost(HttpServletRequest  request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.getWriter()
                    .write("{\"success\":false,\"message\":\"Not logged in.\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        boolean done = notificationDAO.markAllAsRead(user.getUserId());

        // Update session unread count to zero
        session.setAttribute("unreadCount", 0);

        response.getWriter().write(done
            ? "{\"success\":true}"
            : "{\"success\":false}");
    }
}