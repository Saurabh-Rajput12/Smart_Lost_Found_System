package com.lostfound.auth;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * SessionFilter.java
 * ------------------------------------------------
 * Intercepts ALL requests to protected pages.
 * If no valid session exists → redirect to login.
 * Public pages (login, register, assets) are
 * excluded from this check.
 * ------------------------------------------------
 */
@WebFilter("/*")
public class SessionFilter implements Filter {

    // Pages that do NOT require login
    private static final String[] PUBLIC_URLS = {
        "/pages/login.html",
        "/pages/register.html",
        "/login",
        "/register",
        "/assets/",
        "/dbtest",
        "/categories" 
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI()
                             .substring(request.getContextPath().length());

        // Allow public URLs without session check
        for (String publicUrl : PUBLIC_URLS) {
            if (path.startsWith(publicUrl)) {
                chain.doFilter(req, res); // pass through
                return;
            }
        }

        // Allow root path (redirects to welcome file → login.html)
        if (path.equals("/") || path.isEmpty()) {
            chain.doFilter(req, res);
            return;
        }

        // Check for valid session
        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null &&
                            session.getAttribute("user") != null);

        if (loggedIn) {
            chain.doFilter(req, res); // user is logged in, proceed
        } else {
            // No session → redirect to login
            response.sendRedirect(request.getContextPath() +
                "/pages/login.html?error=Please login to continue.");
        }
    }
}