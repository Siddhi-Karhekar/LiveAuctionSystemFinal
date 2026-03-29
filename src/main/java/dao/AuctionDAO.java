package dao;

import model.Auction;
import model.Item;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionDAO - CRUD operations for the auctions table.
 * Also handles status synchronisation (scheduled → active → ended).
 */
public class AuctionDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /** Create a new auction. Returns generated PK. */
    public int insert(Auction auction) throws SQLException {
        String sql = "INSERT INTO auctions (item_id, start_time, end_time, status) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, auction.getItemId());
            ps.setTimestamp(2, auction.getStartTime());
            ps.setTimestamp(3, auction.getEndTime());
            ps.setString(4, "scheduled");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Auction insert failed — no generated key");
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Full auction by PK (joins item + seller + highest bid + bid count). */
    public Auction findById(int id) throws SQLException {
        String sql = buildDetailQuery("a.id = ?");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All active auctions (for buyer browse). */
    public List<Auction> findActive() throws SQLException {
        // First auto-update statuses, then fetch
        syncStatuses();
        String sql = buildDetailQuery("a.status = 'active'") + " ORDER BY a.end_time ASC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            List<Auction> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All auctions for a specific seller. */
    public List<Auction> findBySeller(int sellerId) throws SQLException {
        syncStatuses();
        String sql = buildDetailQuery("i.seller_id = ?") + " ORDER BY a.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sellerId);
            ResultSet rs = ps.executeQuery();
            List<Auction> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All auctions (admin panel). */
    public List<Auction> findAll() throws SQLException {
        syncStatuses();
        String sql = buildDetailQuery(null) + " ORDER BY a.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(sql);
            List<Auction> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Seller selects winning bid and winner. */
    public boolean selectWinner(int auctionId, int winnerId, BigDecimal winningBid) throws SQLException {
        String sql = "UPDATE auctions SET winner_id=?, winning_bid=? WHERE id=? AND status='ended'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, winnerId);
            ps.setBigDecimal(2, winningBid);
            ps.setInt(3, auctionId);
            return ps.executeUpdate() == 1;
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Auto-sync auction statuses based on current time.
     * scheduled → active  when NOW() >= start_time
     * active    → ended   when NOW() > end_time
     */
    public void syncStatuses() throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // Activate scheduled auctions whose start_time has passed
            conn.createStatement().executeUpdate(
                "UPDATE auctions SET status='active' "
              + "WHERE status='scheduled' AND NOW() >= start_time");
            // End active auctions whose end_time has passed
            conn.createStatement().executeUpdate(
                "UPDATE auctions SET status='ended' "
              + "WHERE status='active' AND NOW() > end_time");
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── HELPER: shared SQL ────────────────────────────────────────────────────

    /**
     * Builds a rich SELECT that joins items, users (seller + winner),
     * and aggregates highest bid and total bids.
     * @param whereClause optional WHERE clause (no "WHERE" keyword), or null
     */
    private String buildDetailQuery(String whereClause) {
        String w = (whereClause != null) ? " WHERE " + whereClause : "";
        return "SELECT a.*, "
             + "i.name AS item_name, i.description AS item_desc, "
             + "i.start_price, i.image_path, i.seller_id, "
             + "u.name AS seller_name, "
             + "w.name AS winner_name, "
             + "MAX(b.bid_amount) AS highest_bid, "
             + "COUNT(b.id)       AS total_bids "
             + "FROM auctions a "
             + "JOIN items i ON a.item_id = i.id "
             + "JOIN users u ON i.seller_id = u.id "
             + "LEFT JOIN users w ON a.winner_id = w.id "
             + "LEFT JOIN bids b ON a.id = b.auction_id"
             + w
             + " GROUP BY a.id, i.name, i.description, i.start_price, "
             + "i.image_path, i.seller_id, u.name, w.name";
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private Auction mapRow(ResultSet rs) throws SQLException {
        Auction a = new Auction();
        a.setId(rs.getInt("id"));
        a.setItemId(rs.getInt("item_id"));
        a.setStartTime(rs.getTimestamp("start_time"));
        a.setEndTime(rs.getTimestamp("end_time"));
        a.setStatus(rs.getString("status"));

        int winnerId = rs.getInt("winner_id");
        if (!rs.wasNull()) {
            a.setWinnerId(winnerId);
            a.setWinnerName(rs.getString("winner_name"));
            a.setWinningBid(rs.getBigDecimal("winning_bid"));
        }
        a.setCreatedAt(rs.getTimestamp("created_at"));

        // Populate nested Item
        Item item = new Item();
        item.setId(rs.getInt("item_id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setSellerName(rs.getString("seller_name"));
        item.setName(rs.getString("item_name"));
        item.setDescription(rs.getString("item_desc"));
        item.setStartPrice(rs.getBigDecimal("start_price"));
        item.setImagePath(rs.getString("image_path"));
        a.setItem(item);

        // Aggregate fields
        BigDecimal highBid = rs.getBigDecimal("highest_bid");
        a.setCurrentHighestBid(highBid); // null if no bids yet
        a.setTotalBids(rs.getInt("total_bids"));

        return a;
    }
}
