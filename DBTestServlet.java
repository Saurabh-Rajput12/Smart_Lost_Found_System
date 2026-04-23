package com.lostfound.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DBTestServlet.java
 * ------------------------------------------------------------
 * A temporary test servlet to verify your DB connection works.
 * Visit: http://localhost:8080/LostFoundSystem/dbtest
 *
 * ⚠️  DELETE THIS FILE before going to production!
 *
 * Place: src/main/java/com/lostfound/utils/DBTestServlet.java
 * ------------------------------------------------------------
 */
@WebServlet("/dbtest")
public class DBTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Set response type to HTML so browser renders it nicely
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><body style='font-family:monospace;padding:2rem'>");
        out.println("<h2>🔌 Database Connection Test</h2>");

        try {
            // Attempt to get a connection
            Connection conn = DBConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                out.println("<p style='color:green'>✅ <strong>Connected successfully</strong>"
                    + " to <code>smartlostfound_db</code></p>");

                // Run a quick query to list all tables — proves DB is reachable
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES");

                out.println("<p><strong>Tables found in smartlostfound_db:</strong></p><ul>");
                while (rs.next()) {
                    out.println("<li>" + rs.getString(1) + "</li>");
                }
                out.println("</ul>");

                // Show user count as a sanity check
                rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM users");
                if (rs.next()) {
                    out.println("<p>👤 Users in database: <strong>"
                        + rs.getInt("cnt") + "</strong></p>");
                }

                rs.close();
                stmt.close();

            } else {
                out.println("<p style='color:red'>❌ Connection object is null or closed.</p>");
            }

        } catch (Exception e) {
            out.println("<p style='color:red'>❌ <strong>Connection FAILED</strong></p>");
            out.println("<pre>" + e.getMessage() + "</pre>");
            out.println("<hr><p>Common fixes:</p><ul>");
            out.println("<li>Is MySQL running? Check MySQL Workbench or Services</li>");
            out.println("<li>Is the password correct in <code>DBConfig.java</code>?</li>");
            out.println("<li>Is <code>mysql-connector-j.jar</code> in WEB-INF/lib?</li>");
            out.println("<li>Is the database name exactly <code>smartlostfound_db</code>?</li>");
            out.println("</ul>");
        }

        out.println("</body></html>");
    }
}