package com.lostfound.servlet;

import com.google.gson.Gson;
import com.lostfound.dao.CategoryDAO;
import com.lostfound.models.Category;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * CategoryServlet.java
 * ------------------------------------------------
 * GET  /categories        → returns JSON list of all
 *                           categories (for dropdowns)
 * POST /categories/add    → admin adds a new category
 * ------------------------------------------------
 */
@WebServlet(urlPatterns = {"/categories", "/categories/add"})
public class CategoryServlet extends HttpServlet {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final Gson        gson        = new Gson();

    // GET → return all categories as JSON
    @Override
    protected void doGet(HttpServletRequest  request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // Must be logged in to fetch categories
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.getWriter().write("[]");
            return;
        }

        List<Category> categories = categoryDAO.getAllCategories();
        response.getWriter().write(gson.toJson(categories));
    }

    // POST /categories/add → admin adds new category
    @Override
    protected void doPost(HttpServletRequest  request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // Admin only
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.getWriter()
                    .write("{\"success\":false,\"message\":\"Not logged in.\"}");
            return;
        }

        com.lostfound.models.User admin =
            (com.lostfound.models.User) session.getAttribute("user");

        if (!admin.isAdmin()) {
            response.getWriter()
                    .write("{\"success\":false," +
                           "\"message\":\"Admin access required.\"}");
            return;
        }

        String name = request.getParameter("categoryName");
        if (name == null || name.trim().isEmpty()) {
            response.getWriter()
                    .write("{\"success\":false," +
                           "\"message\":\"Category name is required.\"}");
            return;
        }

        boolean added = categoryDAO.addCategory(name.trim());
        response.getWriter().write(added
            ? "{\"success\":true}"
            : "{\"success\":false,\"message\":\"Failed or already exists.\"}");
    }
}