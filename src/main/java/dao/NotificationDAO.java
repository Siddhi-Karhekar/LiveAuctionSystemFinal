package dao;

import model.Notification;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationDAO - persistence for win/loss notices.
 */
public class NotificationDAO {

    /** Insert a single notification. Returns generated PK. */
    public int insert(Notification n) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, auction_id, type, message) VALUES (?,?,?,?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, n.getUserId());
            ps.setInt(2, n.getAuctionId());
            ps.setString(3, n.getType());
            ps.setString(4, n.getMessage());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Notification insert failed");
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All notifications for a user, newest first (joins item name for context). */
    public List<Notification> findByUser(int userId) throws SQLException {
        String sql = "SELECT n.*, i.name AS item_name "
                   + "FROM notifications n "
                   + "JOIN auctions a ON n.auction_id = a.id "
                   + "JOIN items    i ON a.item_id   = i.id "
                   + "WHERE n.user_id = ? ORDER BY n.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            List<Notification> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** Count of unread notifications for a user (used for nav badge). */
    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** Mark every unread notification for a user as read. */
    public int markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeUpdate();
        } finally {
            DBConnection.close(conn);
        }
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setAuctionId(rs.getInt("auction_id"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getTimestamp("created_at"));
        n.setAuctionItemName(rs.getString("item_name"));
        return n;
    }
}
