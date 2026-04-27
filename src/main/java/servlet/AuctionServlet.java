package servlet;

import dao.AuctionDAO;
import dao.BidDAO;
import dao.NotificationDAO;
import model.Auction;
import model.Bid;
import model.Notification;
import model.User;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * AuctionServlet - list, detail, create, and winner-selection for auctions.
 *
 * URL mappings:
 *   GET  /auction?action=list              → auction_list.jsp (buyer browse)
 *   GET  /auction?action=detail&id=X       → auction_detail.jsp
 *   POST /auction?action=create            → create auction (called by ItemServlet forward)
 *   POST /auction?action=selectWinner&id=X → seller selects winning bidder
 */
@WebServlet("/auction")
public class AuctionServlet extends HttpServlet {

    private final AuctionDAO      auctionDAO      = new AuctionDAO();
    private final BidDAO          bidDAO          = new BidDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // ── GET ───────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":   showList(req, resp);   break;
            case "detail": showDetail(req, resp); break;
            default:       showList(req, resp);
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        if (action == null) action = "create";

        switch (action) {
            case "create":       createAuction(req, resp);   break;
            case "selectWinner": selectWinner(req, resp);    break;
            default:
                resp.sendRedirect(req.getContextPath() + "/auction?action=list");
        }
    }

    // ── HANDLERS ──────────────────────────────────────────────────────────────

    /** Browse active auctions (any logged-in user). */
    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ensureLoggedIn(req, resp);
        try {
            List<Auction> auctions = auctionDAO.findActive();
            req.setAttribute("auctions", auctions);
            req.getRequestDispatcher("/jsp/auction_list.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error fetching auctions", e);
        }
    }

    /** Auction detail + bid history. */
    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ensureLoggedIn(req, resp);
        String idStr = req.getParameter("id");
        if (idStr == null) { resp.sendRedirect(req.getContextPath() + "/auction?action=list"); return; }

        try {
            Auction auction = auctionDAO.findById(Integer.parseInt(idStr));
            if (auction == null) { resp.sendError(404, "Auction not found"); return; }

            List<Bid> bids = bidDAO.findByAuction(auction.getId());
            req.setAttribute("auction", auction);
            req.setAttribute("bids",    bids);
            req.getRequestDispatcher("/jsp/auction_detail.jsp").forward(req, resp);
        } catch (SQLException | NumberFormatException e) {
            throw new ServletException("Error fetching auction detail", e);
        }
    }

    /**
     * Called via forward from ItemServlet after item is saved.
     * Reads itemId, startTime, endTime from request attributes.
     */
    private void createAuction(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null || !user.isSeller()) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        // These attributes are set by ItemServlet before forwarding
        Object itemIdObj   = req.getAttribute("itemId");
        Object startTimeObj = req.getAttribute("startTime");
        Object endTimeObj   = req.getAttribute("endTime");

        if (itemIdObj == null || startTimeObj == null || endTimeObj == null) {
            req.setAttribute("error", "Missing auction time parameters.");
            req.getRequestDispatcher("/jsp/create_auction.jsp").forward(req, resp);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Timestamp start = new Timestamp(sdf.parse(startTimeObj.toString()).getTime());
            Timestamp end   = new Timestamp(sdf.parse(endTimeObj.toString()).getTime());

            if (!end.after(start)) {
                req.setAttribute("error", "End time must be after start time.");
                req.getRequestDispatcher("/jsp/create_auction.jsp").forward(req, resp);
                return;
            }

            Auction auction = new Auction();
            auction.setItemId((int) itemIdObj);
            auction.setStartTime(start);
            auction.setEndTime(end);
            auctionDAO.insert(auction);

            resp.sendRedirect(req.getContextPath() + "/seller?action=dashboard&success=1");

        } catch (ParseException | SQLException e) {
            throw new ServletException("Error creating auction", e);
        }
    }

    /** Seller picks the winning bid after auction ends. Generates win/loss notifications. */
    private void selectWinner(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null || !user.isSeller()) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        String auctionIdStr = req.getParameter("auctionId");
        String bidderIdStr  = req.getParameter("bidderId");
        String amountStr    = req.getParameter("bidAmount");

        try {
            int auctionId   = Integer.parseInt(auctionIdStr);
            int bidderId    = Integer.parseInt(bidderIdStr);
            BigDecimal amount = new BigDecimal(amountStr);

            // Owner check — only the seller who owns the auction (or admin) may pick a winner
            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null) { resp.sendError(404, "Auction not found"); return; }
            if (!user.isAdmin() && auction.getItem().getSellerId() != user.getId()) {
                resp.sendRedirect(req.getContextPath() + "/auction?action=list");
                return;
            }
            if (auction.getWinnerId() != null) {
                resp.sendRedirect(req.getContextPath() + "/auction?action=detail&id=" + auctionId);
                return;
            }

            boolean ok = auctionDAO.selectWinner(auctionId, bidderId, amount);
            if (ok) {
                notifyBidders(auctionId, bidderId);
            }
            resp.sendRedirect(req.getContextPath() + "/auction?action=detail&id=" + auctionId + "&msg=winner_set");
        } catch (Exception e) {
            throw new ServletException("Error selecting winner", e);
        }
    }

    /**
     * Insert one notification per unique bidder on this auction:
     *   winner   → "You won the bid!"
     *   others   → "You did not win the bid"
     */
    private void notifyBidders(int auctionId, int winnerId) throws java.sql.SQLException {
        List<Bid> bids = bidDAO.findByAuction(auctionId);
        Set<Integer> seen = new HashSet<>();
        for (Bid b : bids) {
            if (!seen.add(b.getBidderId())) continue; // one notification per user

            Notification n = new Notification();
            n.setUserId(b.getBidderId());
            n.setAuctionId(auctionId);
            if (b.getBidderId() == winnerId) {
                n.setType("win");
                n.setMessage("You won the bid!");
            } else {
                n.setType("loss");
                n.setMessage("You did not win the bid");
            }
            notificationDAO.insert(n);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void ensureLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (getSessionUser(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
        }
    }

    private User getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
