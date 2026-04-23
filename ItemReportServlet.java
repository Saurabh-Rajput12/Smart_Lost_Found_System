package com.lostfound.servlet;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.MatchDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.models.Item;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ItemReportServlet.java
 * ------------------------------------------------
 * POST /item/report
 * Handles new lost/found item submissions.
 * Supports image upload (stored as file path).
 * After saving, runs smart match check and
 * sends notifications if matches are found.
 * ------------------------------------------------
 */
@WebServlet("/item/report")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,      // 1 MB — write to disk after this
    maxFileSize       = 5 * 1024 * 1024,  // 5 MB max per file
    maxRequestSize    = 10 * 1024 * 1024  // 10 MB max total request
)
public class ItemReportServlet extends HttpServlet {

    private final ItemDAO         itemDAO         = new ItemDAO();
    private final MatchDAO        matchDAO        = new MatchDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

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

        // ── Read form fields ──────────────────────────────
        String title       = request.getParameter("title");
        String description = request.getParameter("description");
        String type        = request.getParameter("type");
        String categoryStr = request.getParameter("categoryId");
        String location    = request.getParameter("location");
        String dateStr     = request.getParameter("dateOccurred");

        // ── Server-side validation ────────────────────────
        if (title == null || title.trim().isEmpty()) {
            out.write("{\"success\":false,\"message\":\"Title is required.\"}");
            return;
        }
        if (type == null || (!type.equals("LOST") && !type.equals("FOUND"))) {
            out.write("{\"success\":false,\"message\":\"Invalid item type.\"}");
            return;
        }
        if (location == null || location.trim().isEmpty()) {
            out.write("{\"success\":false,\"message\":\"Location is required.\"}");
            return;
        }

        int categoryId = 0;
        try {
            categoryId = Integer.parseInt(categoryStr);
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"Please select a category.\"}");
            return;
        }

        // ── Parse date ────────────────────────────────────
        LocalDateTime dateOccurred = LocalDateTime.now();
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                dateOccurred = LocalDateTime.parse(
                    dateStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                );
            } catch (Exception e) {
                // Use current time if parsing fails
                dateOccurred = LocalDateTime.now();
            }
        }

        // ── Handle image upload ───────────────────────────
        String imagePath = null;
        try {
            Part filePart = request.getPart("image");

            if (filePart != null && filePart.getSize() > 0) {
                String fileName = getSubmittedFileName(filePart);

                if (fileName != null && !fileName.isEmpty()) {
                    // Validate file type — images only
                    String ext = fileName.substring(
                        fileName.lastIndexOf('.') + 1
                    ).toLowerCase();

                    if (!ext.matches("jpg|jpeg|png|gif|webp")) {
                        out.write("{\"success\":false," +
                                  "\"message\":\"Only image files allowed.\"}");
                        return;
                    }

                    // Save to webapp/assets/uploads/
                    String uploadDir = getServletContext()
                        .getRealPath("/assets/uploads");

                    // Create directory if it doesn't exist
                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    // Create unique filename to prevent conflicts
                    String uniqueName = System.currentTimeMillis()
                                      + "_" + userId
                                      + "." + ext;

                    filePart.write(uploadDir + File.separator + uniqueName);

                    // Store relative path (used in <img src="...">)
                    imagePath = "assets/uploads/" + uniqueName;
                }
            }
        } catch (Exception e) {
            System.err.println("[ItemReport] Image upload error: "
                               + e.getMessage());
            // Continue without image — not mandatory
        }

        // ── Build Item object ─────────────────────────────
        Item item = new Item();
        item.setTitle(title.trim());
        item.setDescription(description != null ? description.trim() : "");
        item.setType(type);
        item.setStatus(type);          // LOST item gets status=LOST, etc.
        item.setCategoryId(categoryId);
        item.setLocation(location.trim());
        item.setDateOccurred(dateOccurred);
        item.setImagePath(imagePath);
        item.setReportedBy(userId);

        // ── Save to database ──────────────────────────────
        boolean saved = itemDAO.reportItem(item);

        if (!saved) {
            out.write("{\"success\":false," +
                      "\"message\":\"Failed to save report. Please try again.\"}");
            return;
        }

        // ── Run Smart Matching after save ─────────────────
        // Fetch the saved item (with its generated ID + refCode)
        // then look for matching items of the opposite type
        try {
            // Get the newly created item by searching recent items
            List<Item> userItems = itemDAO.getItemsByUser(userId);
            if (!userItems.isEmpty()) {
                Item savedItem = userItems.get(0); // most recent = first

                List<Item> matches = itemDAO.findMatches(savedItem);

                for (Item match : matches) {
                    // Save each match pair to matches table
                    if (type.equals("LOST")) {
                        matchDAO.saveMatch(savedItem.getItemId(),
                                           match.getItemId());
                    } else {
                        matchDAO.saveMatch(match.getItemId(),
                                           savedItem.getItemId());
                    }

                    // Notify the reporter of the matched item
                    String msg = "✨ Smart Match Found! Your item '"
                               + match.getTitle()
                               + "' (" + match.getRefCode() + ")"
                               + " may match a newly reported "
                               + (type.equals("LOST") ? "found" : "lost")
                               + " item: '"
                               + savedItem.getTitle() + "'.";

                    notificationDAO.createNotification(
                        match.getReportedBy(), msg
                    );

                    // Also notify current user
                    String myMsg = "✨ Smart Match Found for your "
                                 + type.toLowerCase()
                                 + " item! Check the Matches tab.";
                    notificationDAO.createNotification(userId, myMsg);
                }
            }
        } catch (Exception e) {
            System.err.println("[ItemReport] Matching error: "
                               + e.getMessage());
            // Non-critical — item is already saved
        }

        out.write("{\"success\":true," +
                  "\"message\":\"Item reported successfully!\"}");
    }

    // ── Helper: extract original filename from Part ────────
    private String getSubmittedFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;

        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 1)
                                   .trim()
                                   .replace("\"", "");
                // Handle Windows paths
                return name.substring(name.lastIndexOf('/') + 1)
                           .substring(name.lastIndexOf('\\') + 1);
            }
        }
        return null;
    }
}