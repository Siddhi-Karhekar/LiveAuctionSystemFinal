package servlet;

import dao.UserDAO;
import model.User;
import util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * AuthenticationServlet - handles login, logout, and registration.
 *
 * URL mappings:
 *   GET  /auth?action=login     → login.jsp
 *   POST /auth?action=login     → authenticate and redirect
 *   GET  /auth?action=register  → register.jsp
 *   POST /auth?action=register  → create account and redirect
 *   GET  /auth?action=logout    → invalidate session and redirect
 */
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "login";

        switch (action) {
            case "logout":
                req.getSession(false).invalidate();
                resp.sendRedirect(req.getContextPath() + "/auth?action=login");
                break;

            case "register":
                req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
                break;

            default: // "login"
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
                break;
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "login";

        switch (action) {
            case "login":    handleLogin(req, resp);    break;
            case "register": handleRegister(req, resp); break;
            default:
                resp.sendRedirect(req.getContextPath() + "/auth?action=login");
        }
    }

    // ── PRIVATE HANDLERS ──────────────────────────────────────────────────────

    /** Validate credentials and create a session. */
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        if (!ValidationUtil.isNotBlank(email) || !ValidationUtil.isNotBlank(password)) {
            req.setAttribute("error", "Email and password are required.");
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userDAO.login(email.trim(), password);
            if (user == null) {
                req.setAttribute("error", "Invalid email or password.");
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);
                return;
            }

            // Create session
            HttpSession session = req.getSession(true);
            session.setAttribute("loggedUser", user);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Redirect based on role
            if (user.isAdmin()) {
                resp.sendRedirect(req.getContextPath() + "/seller?action=dashboard");
            } else if (user.isSeller()) {
                resp.sendRedirect(req.getContextPath() + "/seller?action=dashboard");
            } else {
                // Buyers land on the live auction list
                resp.sendRedirect(req.getContextPath() + "/auction?action=list");
            }

        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    /** Register a new user account. */
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name     = req.getParameter("name");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String role     = req.getParameter("role");

        // Server-side validation
        if (!ValidationUtil.isNotBlank(name) || !ValidationUtil.isValidEmail(email)
                || !ValidationUtil.isNotBlank(password) || !ValidationUtil.isNotBlank(role)) {
            req.setAttribute("error", "All fields are required and email must be valid.");
            req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
            return;
        }
        if (password.length() < 6) {
            req.setAttribute("error", "Password must be at least 6 characters.");
            req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
            return;
        }
        if (!role.equals("buyer") && !role.equals("seller")) {
            req.setAttribute("error", "Invalid role selected.");
            req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
            return;
        }

        try {
            // Check for duplicate email
            if (userDAO.findByEmail(email.trim()) != null) {
                req.setAttribute("error", "An account with that email already exists.");
                req.getRequestDispatcher("/jsp/register.jsp").forward(req, resp);
                return;
            }

            User newUser = new User();
            newUser.setName(name.trim());
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPassword(password); // DAO will hash this
            newUser.setRole(role);

            userDAO.register(newUser);
            req.setAttribute("success", "Account created! Please log in.");
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error during registration", e);
        }
    }
}
