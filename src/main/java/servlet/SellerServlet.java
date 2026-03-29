package servlet;

import dao.AuctionDAO;
import model.Auction;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * SellerServlet - seller dashboard (lists seller's own auctions).
 *
 * URL mappings:
 *   GET /seller?action=dashboard → seller_dashboard.jsp
 */
@WebServlet("/seller")
public class SellerServlet extends HttpServlet {

    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null || (!user.isSeller() && !user.isAdmin())) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        try {
            List<Auction> auctions = user.isAdmin()
                ? auctionDAO.findAll()
                : auctionDAO.findBySeller(user.getId());

            req.setAttribute("auctions", auctions);
            req.getRequestDispatcher("/jsp/seller_dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error loading seller dashboard", e);
        }
    }

    private User getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
