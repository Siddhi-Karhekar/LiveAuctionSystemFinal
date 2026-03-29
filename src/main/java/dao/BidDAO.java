package dao;

import model.Bid;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BidDAO - operations on the bids table.
 */
public class BidDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Place a new bid. Returns generated PK.
     * Does NOT validate amount here — validation is in BidServlet.
     */
    public int placeBid(Bid bid) throws SQLException {
        String sql = "INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES (?,?,?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bid.getAuctionId());
            ps.setInt(2, bid.getBidderId());
            ps.setBigDecimal(3, bid.getBidAmount());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Bid insert failed — no generated key");
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /**
     * All bids for a given auction, newest first. Joins bidder name.
     */
    public List<Bid> findByAuction(int auctionId) throws SQLException {
        String sql = "SELECT b.*, u.name AS bidder_name FROM bids b "
                   + "JOIN users u ON b.bidder_id = u.id "
                   + "WHERE b.auction_id = ? ORDER BY b.bid_amount DESC, b.bid_time DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();
            List<Bid> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** Current highest bid amount for an auction (null if no bids). */
    public BigDecimal getHighestBid(int auctionId) throws SQLException {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1); // may be null
            return null;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** Highest bid placed by a specific bidder on an auction. */
    public BigDecimal getHighestBidByUser(int auctionId, int userId) throws SQLException {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id=? AND bidder_id=?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, auctionId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
            return null;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All bids placed by a user (for buyer history). */
    public List<Bid> findByBidder(int bidderId) throws SQLException {
        String sql = "SELECT b.*, u.name AS bidder_name FROM bids b "
                   + "JOIN users u ON b.bidder_id = u.id "
                   + "WHERE b.bidder_id = ? ORDER BY b.bid_time DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bidderId);
            ResultSet rs = ps.executeQuery();
            List<Bid> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private Bid mapRow(ResultSet rs) throws SQLException {
        Bid b = new Bid();
        b.setId(rs.getInt("id"));
        b.setAuctionId(rs.getInt("auction_id"));
        b.setBidderId(rs.getInt("bidder_id"));
        b.setBidderName(rs.getString("bidder_name"));
        b.setBidAmount(rs.getBigDecimal("bid_amount"));
        b.setBidTime(rs.getTimestamp("bid_time"));
        return b;
    }
}
