package com.lostfound.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.MatchDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.models.Claim;
import com.lostfound.models.Item;
import com.lostfound.models.Match;
import com.lostfound.models.Notification;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/user/dashboard")
public class UserDashboardServlet extends HttpServlet {

    private final ItemDAO         itemDAO         = new ItemDAO();
    private final ClaimDAO        claimDAO        = new ClaimDAO();
    private final MatchDAO        matchDAO        = new MatchDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Gson with LocalDateTime support
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

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user   = (User) session.getAttribute("user");
        int  userId = user.getUserId();

        Map<String, Object> data = new HashMap<>();

        try {
            List<Item> myItems = itemDAO.getItemsByUser(userId);
            data.put("myItems", myItems);

            long myLost  = myItems.stream()
                                  .filter(i -> "LOST".equals(i.getType()))
                                  .count();
            long myFound = myItems.stream()
                                  .filter(i -> "FOUND".equals(i.getType()))
                                  .count();
            data.put("myLostCount",  myLost);
            data.put("myFoundCount", myFound);

            List<Claim> myClaims = claimDAO.getClaimsByUser(userId);
            data.put("myClaims", myClaims);

            long pendingClaims  = myClaims.stream().filter(Claim::isPending).count();
            long approvedClaims = myClaims.stream().filter(Claim::isApproved).count();
            data.put("pendingClaimsCount",  pendingClaims);
            data.put("approvedClaimsCount", approvedClaims);

            List<Match> myMatches = matchDAO.getMatchesForUser(userId);
            data.put("myMatches",  myMatches);
            data.put("matchCount", myMatches.size());

            List<Notification> notifications =
                notificationDAO.getNotificationsByUser(userId);
            data.put("notifications", notifications);
            data.put("unreadCount",
                     notificationDAO.getUnreadCount(userId));

            data.put("userName", user.getName());

        } catch (Exception e) {
            System.err.println("[UserDashboard] Error: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        response.getWriter().write(gson.toJson(data));
    }
}