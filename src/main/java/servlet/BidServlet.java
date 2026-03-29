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
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * BidServlet - handles bid placement and real-time-like status polling.
 *
 * URL mappings:
 *   POST /bid?action=place          → place a bid (HTML form submit)
 *   GET  /bid?action=status&id=X    → JSON response with current highest bid (AJAX poll)
 *   GET  /bid?action=history&id=X   → bid_history.jsp
 */
@WebServlet("/bid")
public class BidServlet extends HttpServlet {

    private final BidDAO     bidDAO     = new BidDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "history";

        switch (action) {
            case "status":  sendStatus(req, resp);  break;
            case "history": showHistory(req, resp); break;
            default:
                resp.sendRedirect(req.getContextPath() + "/auction?action=list");
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        placeBid(req, resp);
    }

    // ── HANDLERS ──────────────────────────────────────────────────────────────

    /**
     * Place a bid. Validates:
     *  1. User is logged in as buyer.
     *  2. Auction exists and is currently active.
     *  3. Bid amount > current highest bid (or start price if no bids yet).
     */
    private void placeBid(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }
        if (user.isSeller() && !user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/auction?action=list&error=sellers_cannot_bid");
            return;
        }

        String auctionIdStr = req.getParameter("auctionId");
        String amountStr    = req.getParameter("bidAmount");

        try {
            int auctionId = Integer.parseInt(auctionIdStr);
            Auction auction = auctionDAO.findById(auctionId);

            if (auction == null) {
                resp.sendRedirect(req.getContextPath() + "/auction?action=list&error=not_found");
                return;
            }
            if (!auction.isActive()) {
                resp.sendRedirect(req.getContextPath() + "/auction?action=detail&id=" + auctionId + "&error=not_active");
                return;
            }

            BigDecimal bidAmount = new BigDecimal(amountStr);
            BigDecimal minBid    = auction.getMinimumNextBid();

            if (bidAmount.compareTo(minBid) < 0) {
                resp.sendRedirect(req.getContextPath()
                    + "/auction?action=detail&id=" + auctionId
                    + "&error=bid_too_low&min=" + minBid.toPlainString());
                return;
            }

            Bid bid = new Bid(auctionId, user.getId(), bidAmount);
            bidDAO.placeBid(bid);

            resp.sendRedirect(req.getContextPath()
                + "/auction?action=detail&id=" + auctionId + "&success=bid_placed");

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/auction?action=list&error=invalid_data");
        } catch (SQLException e) {
            throw new ServletException("Database error placing bid", e);
        }
    }

    /**
     * AJAX endpoint — returns lightweight JSON with current auction status.
     * Polled every ~5 seconds from auction_detail.jsp JavaScript.
     *
     * Response: { "highestBid": 3200.00, "totalBids": 4, "status": "active" }
     */
    private void sendStatus(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String idStr = req.getParameter("id");
        if (idStr == null) { out.print("{\"error\":\"Missing id\"}"); return; }

        try {
            // Sync statuses then fetch
            auctionDAO.syncStatuses();
            Auction auction = auctionDAO.findById(Integer.parseInt(idStr));
            if (auction == null) { out.print("{\"error\":\"Not found\"}"); return; }

            BigDecimal high = auction.getCurrentHighestBid();
            String highStr  = (high != null) ? high.toPlainString() : "null";

            out.printf("{\"highestBid\":%s,\"totalBids\":%d,\"status\":\"%s\"}",
                highStr, auction.getTotalBids(), auction.getStatus());

        } catch (SQLException | NumberFormatException e) {
            out.print("{\"error\":\"Server error\"}");
        }
    }

    /** Bid history for an auction — bid_history.jsp. */
    private void showHistory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/auth?action=login"); return; }

        String idStr = req.getParameter("id");
        try {
            int auctionId   = Integer.parseInt(idStr);
            Auction auction = auctionDAO.findById(auctionId);
            List<Bid> bids  = bidDAO.findByAuction(auctionId);
            req.setAttribute("auction", auction);
            req.setAttribute("bids",    bids);
            req.getRequestDispatcher("/jsp/bid_history.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException("Error loading bid history", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
