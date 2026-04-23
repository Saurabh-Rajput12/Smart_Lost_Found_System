package com.lostfound.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lostfound.dao.ItemDAO;
import com.lostfound.models.Item;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ItemSearchServlet.java
 * ------------------------------------------------
 * GET /item/search?keyword=...&categoryId=...
 *     &type=...&status=...&location=...
 *     &dateFrom=...&dateTo=...
 *
 * Returns JSON array of matching items.
 * Used by the search section in user dashboard.
 * ------------------------------------------------
 */
@WebServlet("/item/search")
public class ItemSearchServlet extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();

    // Gson with LocalDateTime adapter (Java 8+ time)
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime.class,
            (com.google.gson.JsonSerializer<LocalDateTime>)
                (src, type, ctx) ->
                    new com.google.gson.JsonPrimitive(src.toString())
        )
        .create();

    @Override
    protected void doGet(HttpServletRequest  request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // ── Session check ─────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.getWriter().write("[]");
            return;
        }

        // ── Read filter parameters ────────────────────────
        String keyword    = request.getParameter("keyword");
        String categoryId = request.getParameter("categoryId");
        String type       = request.getParameter("type");
        String status     = request.getParameter("status");
        String location   = request.getParameter("location");
        String dateFrom   = request.getParameter("dateFrom");
        String dateTo     = request.getParameter("dateTo");

        // Parse categoryId (0 = all)
        int catId = 0;
        try {
            if (categoryId != null && !categoryId.isEmpty()) {
                catId = Integer.parseInt(categoryId);
            }
        } catch (NumberFormatException e) {
            catId = 0;
        }

        // ── Perform search ────────────────────────────────
        List<Item> results = itemDAO.searchItems(
            keyword, catId, type, status,
            location, dateFrom, dateTo
        );

        // ── Return JSON ───────────────────────────────────
        response.getWriter().write(gson.toJson(results));
    }
}