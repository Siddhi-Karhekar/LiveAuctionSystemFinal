package servlet;

import dao.NotificationDAO;
import model.Notification;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * NotificationServlet — buyer's win/loss notifications.
 *
 *   GET  /notification?action=list     → notifications.jsp
 *   POST /notification?action=markRead → mark all unread, redirect back to list
 */
@WebServlet("/notification")
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/auth?action=login"); return; }

        try {
            List<Notification> notifications = notificationDAO.findByUser(user.getId());
            req.setAttribute("notifications", notifications);
            req.getRequestDispatcher("/jsp/notifications.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error loading notifications", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = currentUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/auth?action=login"); return; }

        String action = req.getParameter("action");
        if ("markRead".equals(action)) {
            try {
                notificationDAO.markAllRead(user.getId());
            } catch (SQLException e) {
                throw new ServletException("Error marking notifications read", e);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/notification?action=list");
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return (s != null) ? (User) s.getAttribute("loggedUser") : null;
    }
}
