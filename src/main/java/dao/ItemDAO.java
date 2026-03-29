package dao;

import model.Item;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemDAO - CRUD operations for the items table.
 */
public class ItemDAO {

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Insert a new item. Returns the generated primary key.
     */
    public int insert(Item item) throws SQLException {
        String sql = "INSERT INTO items (seller_id, name, description, start_price, image_path) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getSellerId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getDescription());
            ps.setBigDecimal(4, item.getStartPrice());
            ps.setString(5, item.getImagePath());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
            throw new SQLException("Item insert failed — no generated key");
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Find item by PK (joins seller name). */
    public Item findById(int id) throws SQLException {
        String sql = "SELECT i.*, u.name AS seller_name FROM items i "
                   + "JOIN users u ON i.seller_id = u.id WHERE i.id = ?";
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

    /** All items belonging to a seller. */
    public List<Item> findBySeller(int sellerId) throws SQLException {
        String sql = "SELECT i.*, u.name AS seller_name FROM items i "
                   + "JOIN users u ON i.seller_id = u.id "
                   + "WHERE i.seller_id = ? ORDER BY i.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sellerId);
            ResultSet rs = ps.executeQuery();
            List<Item> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    /** All items (admin / browse). */
    public List<Item> findAll() throws SQLException {
        String sql = "SELECT i.*, u.name AS seller_name FROM items i "
                   + "JOIN users u ON i.seller_id = u.id ORDER BY i.created_at DESC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<Item> list = new ArrayList<>();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Update item details (name, description, start_price, image_path). */
    public boolean update(Item item) throws SQLException {
        String sql = "UPDATE items SET name=?, description=?, start_price=?, image_path=? WHERE id=? AND seller_id=?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBigDecimal(3, item.getStartPrice());
            ps.setString(4, item.getImagePath());
            ps.setInt(5, item.getId());
            ps.setInt(6, item.getSellerId());
            return ps.executeUpdate() == 1;
        } finally {
            DBConnection.close(conn);
        }
    }

    // ── MAPPING ───────────────────────────────────────────────────────────────

    private Item mapRow(ResultSet rs) throws SQLException {
        Item i = new Item();
        i.setId(rs.getInt("id"));
        i.setSellerId(rs.getInt("seller_id"));
        i.setSellerName(rs.getString("seller_name"));
        i.setName(rs.getString("name"));
        i.setDescription(rs.getString("description"));
        i.setStartPrice(rs.getBigDecimal("start_price"));
        i.setImagePath(rs.getString("image_path"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        return i;
    }
}
