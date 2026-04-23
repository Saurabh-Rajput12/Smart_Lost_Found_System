package com.lostfound.servlet;

import com.lostfound.dao.ItemDAO;
import com.lostfound.models.Item;
import com.lostfound.models.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * ReportExportServlet.java
 * ------------------------------------------------
 * GET /report/export?from=...&to=...&status=...
 * Admin-only: exports filtered items as a CSV file.
 * Browser triggers a file download automatically.
 * ------------------------------------------------
 */
@WebServlet("/report/export")
public class ReportExportServlet extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();

    @Override
    protected void doGet(HttpServletRequest  request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Admin check ───────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("../pages/login.html");
            return;
        }

        User admin = (User) session.getAttribute("user");
        if (!admin.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // ── Read filter parameters ────────────────────────
        String dateFrom = request.getParameter("from");
        String dateTo   = request.getParameter("to");
        String status   = request.getParameter("status");

        // ── Fetch filtered items ──────────────────────────
        List<Item> items = itemDAO.searchItems(
            null,       // no keyword
            0,          // all categories
            null,       // all types
            (status != null && !status.isEmpty()) ? status : null,
            null,       // all locations
            (dateFrom != null && !dateFrom.isEmpty()) ? dateFrom : null,
            (dateTo   != null && !dateTo.isEmpty())   ? dateTo   : null
        );

        // ── Set response as downloadable CSV ──────────────
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"lostfound_report.csv\"");

        PrintWriter writer = response.getWriter();

        // ── Write CSV header row ──────────────────────────
        writer.println(
            "Ref Code,Title,Type,Category,Status," +
            "Location,Date Occurred,Reporter,Created At"
        );

        // ── Write data rows ───────────────────────────────
        for (Item item : items) {
            writer.println(
                escapeCsv(item.getRefCode())      + "," +
                escapeCsv(item.getTitle())         + "," +
                escapeCsv(item.getType())          + "," +
                escapeCsv(item.getCategoryName())  + "," +
                escapeCsv(item.getStatus())        + "," +
                escapeCsv(item.getLocation())      + "," +
                escapeCsv(item.getDateOccurred() != null
                    ? item.getDateOccurred().toString() : "") + "," +
                escapeCsv(item.getReporterName())  + "," +
                escapeCsv(item.getCreatedAt() != null
                    ? item.getCreatedAt().toString() : "")
            );
        }

        writer.flush();
    }

    /**
     * Escapes a value for safe CSV output.
     * Wraps in double-quotes if it contains comma,
     * newline, or double-quote characters.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        // If value contains comma, quote, or newline → wrap in quotes
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}