package com.lostfound.servlet;

import com.google.gson.Gson;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.UserDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
/**
 * AdminDashboardServlet.java
 * ------------------------------------------------
 * GET  /admin/dashboard → returns JSON stats for
 *      the admin dashboard (fetched via AJAX).
 * Only accessible by users with role = ADMIN.
 * ------------------------------------------------
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final ItemDAO         itemDAO         = new ItemDAO();
    private final UserDAO         userDAO         = new UserDAO();
    private final ClaimDAO        claimDAO        = new ClaimDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final Gson gson = new GsonBuilder()
    	    .registerTypeAdapter(
    	        LocalDateTime.class,
    	        (JsonSerializer<LocalDateTime>)
    	            (src, type, ctx) -> new JsonPrimitive(src.toString())
    	    )
    	    .serializeNulls()
    	    .create();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Security: admin only ──────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        if (!sessionUser.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                               "Admin access required.");
            return;
        }

        // ── Gather all dashboard data ─────────────────────────
        Map<String, Object> data = new HashMap<>();

        // Stat cards
        data.put("totalLost",    itemDAO.getItemCountByType("LOST"));
        data.put("totalFound",   itemDAO.getItemCountByType("FOUND"));
        data.put("totalClaimed", itemDAO.getItemCountByStatus("CLAIMED"));
        data.put("totalExpired", itemDAO.getItemCountByStatus("EXPIRED"));
        data.put("totalUsers",   userDAO.getTotalUsers());
        data.put("pendingClaims",claimDAO.getPendingClaimCount());

        // Pending claims list (for the table)
        List<Claim> pendingClaims = claimDAO.getPendingClaims();
        data.put("claims", pendingClaims);

        // Recent 8 items (activity feed)
        List<Item> recentItems = itemDAO.getRecentItems(8);
        data.put("recentItems", recentItems);

        // All users (user management table)
        List<User> users = userDAO.getAllUsers();
        // Remove passwords before sending to frontend
        users.forEach(u -> u.setPassword(""));
        data.put("users", users);

        // Admin name for greeting
        data.put("adminName", sessionUser.getName());
        data.put("unreadCount",
                 notificationDAO.getUnreadCount(sessionUser.getUserId()));

        // ── Send as JSON ──────────────────────────────────────
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
}