package servlet;

import dao.AuctionDAO;
import dao.BidDAO;
import model.Auction;
import model.Bid;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BuyerServlet — buyer's personal dashboard.
 *
 * URL mappings:
 *   GET /buyer?action=dashboard → buyer_dashboard.jsp
 *       Shows: all bids placed by this buyer + auctions they won
 */
@WebServlet("/buyer")
public class BuyerServlet extends HttpServlet {

    private final BidDAO     bidDAO     = new BidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        try {
            // Load all bids placed by this buyer
            List<Bid> myBids = bidDAO.findByBidder(user.getId());

            // Find auctions where this buyer was selected as winner
            List<Auction> allAuctions  = auctionDAO.findAll();
            List<Auction> wonAuctions  = new ArrayList<>();
            for (Auction a : allAuctions) {
                if (a.getWinnerId() != null && a.getWinnerId() == user.getId()) {
                    wonAuctions.add(a);
                }
            }

            req.setAttribute("myBids",     myBids);
            req.setAttribute("wonAuctions", wonAuctions);
            req.getRequestDispatcher("/jsp/buyer_dashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Error loading buyer dashboard", e);
        }
    }
}
