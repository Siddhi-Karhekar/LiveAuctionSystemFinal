package swing;

import dao.AuctionDAO;
import dao.BidDAO;
import dao.UserDAO;
import model.Auction;
import model.Bid;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * AdminPanel — Swing desktop application for admin/seller oversight.
 *
 * Features:
 *  - View all auctions with live status
 *  - View bids per auction
 *  - Approve/set winner on ended auctions
 *  - Basic user list
 *
 * Run standalone: java -cp [classpath] swing.AdminPanel
 * Requires MySQL JDBC driver on classpath.
 */
public class AdminPanel extends JFrame {

    // ── DAO instances ─────────────────────────────────────────────────────────
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final BidDAO     bidDAO     = new BidDAO();
    private final UserDAO    userDAO    = new UserDAO();

    // ── UI Components ─────────────────────────────────────────────────────────
    private JTable auctionTable;
    private JTable bidTable;
    private JTable userTable;
    private DefaultTableModel auctionModel;
    private DefaultTableModel bidModel;
    private DefaultTableModel userModel;
    private JLabel statusBar;

    // ── Colors / Theme ────────────────────────────────────────────────────────
    private static final Color BG_DARK   = new Color(26, 26, 46);
    private static final Color BG_PANEL  = new Color(22, 33, 62);
    private static final Color ACCENT    = new Color(233, 69, 96);
    private static final Color GOLD      = new Color(240, 165, 0);
    private static final Color TEXT      = new Color(240, 240, 240);
    private static final Color MUTED     = new Color(160, 160, 176);

    public AdminPanel() {
        super("AuctionLive — Admin Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        initUI();
        loadAuctions();
        loadUsers();
    }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void initUI() {
        // Main layout
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT));

        JLabel brand = new JLabel("🔨 AuctionLive  Admin Panel");
        brand.setFont(new Font("Serif", Font.BOLD, 20));
        brand.setForeground(TEXT);
        header.add(brand);

        JButton refreshBtn = styledButton("⟳ Refresh", ACCENT);
        refreshBtn.addActionListener(e -> { loadAuctions(); loadUsers(); });
        header.add(refreshBtn);

        add(header, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("📦 Auctions", buildAuctionTab());
        tabs.addTab("📊 Bids",     buildBidTab());
        tabs.addTab("👤 Users",    buildUserTab());

        add(tabs, BorderLayout.CENTER);

        // Status bar
        statusBar = new JLabel("  Ready");
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusBar.setForeground(MUTED);
        statusBar.setBackground(BG_PANEL);
        statusBar.setOpaque(true);
        statusBar.setBorder(new EmptyBorder(6, 12, 6, 12));
        add(statusBar, BorderLayout.SOUTH);
    }

    // ── Auction Tab ───────────────────────────────────────────────────────────

    private JPanel buildAuctionTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Table
        String[] cols = {"ID", "Item", "Seller", "Start Price", "Highest Bid",
                         "Bids", "Status", "Ends At", "Winner"};
        auctionModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        auctionTable = styledTable(auctionModel);

        JScrollPane scroll = new JScrollPane(auctionTable);
        styleScroll(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(BG_DARK);

        JButton viewBidsBtn   = styledButton("📊 View Bids", new Color(52, 152, 219));
        JButton syncBtn       = styledButton("🔄 Sync Statuses", new Color(39, 174, 96));
        JButton setWinnerBtn  = styledButton("🏆 Set Winner", GOLD);

        viewBidsBtn.addActionListener(e  -> loadBidsForSelected());
        syncBtn.addActionListener(e      -> syncStatuses());
        setWinnerBtn.addActionListener(e -> openSetWinnerDialog());

        btnRow.add(viewBidsBtn);
        btnRow.add(syncBtn);
        btnRow.add(setWinnerBtn);

        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    // ── Bid Tab ───────────────────────────────────────────────────────────────

    private JPanel buildBidTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"Bid ID", "Auction ID", "Bidder", "Amount", "Time"};
        bidModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bidTable = styledTable(bidModel);

        JScrollPane scroll = new JScrollPane(bidTable);
        styleScroll(scroll);

        JLabel hint = new JLabel("  Select an auction in the Auctions tab and click 'View Bids'");
        hint.setForeground(MUTED);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));

        panel.add(hint, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── User Tab ──────────────────────────────────────────────────────────────

    private JPanel buildUserTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        String[] cols = {"ID", "Name", "Email", "Role", "Registered"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = styledTable(userModel);

        JScrollPane scroll = new JScrollPane(userTable);
        styleScroll(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    private void loadAuctions() {
        SwingWorker<List<Auction>, Void> worker = new SwingWorker<>() {
            @Override protected List<Auction> doInBackground() throws Exception {
                return auctionDAO.findAll();
            }
            @Override protected void done() {
                try {
                    List<Auction> auctions = get();
                    auctionModel.setRowCount(0);
                    for (Auction a : auctions) {
                        auctionModel.addRow(new Object[]{
                            a.getId(),
                            a.getItem().getName(),
                            a.getItem().getSellerName(),
                            "₹" + String.format("%,.2f", a.getItem().getStartPrice()),
                            a.getCurrentHighestBid() != null
                                ? "₹" + String.format("%,.2f", a.getCurrentHighestBid()) : "—",
                            a.getTotalBids(),
                            a.getStatus().toUpperCase(),
                            a.getEndTime() != null ? a.getEndTime().toString().substring(0,16) : "—",
                            a.getWinnerName() != null ? "🏆 " + a.getWinnerName() : "—"
                        });
                    }
                    setStatus("Loaded " + auctions.size() + " auctions.");
                } catch (Exception ex) {
                    showError("Error loading auctions: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadBidsForSelected() {
        int row = auctionTable.getSelectedRow();
        if (row < 0) { showError("Please select an auction first."); return; }

        int auctionId = (int) auctionModel.getValueAt(row, 0);

        SwingWorker<List<Bid>, Void> worker = new SwingWorker<>() {
            @Override protected List<Bid> doInBackground() throws Exception {
                return bidDAO.findByAuction(auctionId);
            }
            @Override protected void done() {
                try {
                    List<Bid> bids = get();
                    bidModel.setRowCount(0);
                    for (Bid b : bids) {
                        bidModel.addRow(new Object[]{
                            b.getId(),
                            b.getAuctionId(),
                            b.getBidderName(),
                            "₹" + String.format("%,.2f", b.getBidAmount()),
                            b.getBidTime()
                        });
                    }
                    // Switch to bids tab (index 1)
                    ((JTabbedPane) ((JPanel) auctionTable.getParent().getParent()
                        .getParent()).getParent()).setSelectedIndex(1);
                    setStatus("Showing " + bids.size() + " bids for auction #" + auctionId);
                } catch (Exception ex) {
                    showError("Error loading bids: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override protected List<User> doInBackground() throws Exception {
                return userDAO.findAll();
            }
            @Override protected void done() {
                try {
                    List<User> users = get();
                    userModel.setRowCount(0);
                    for (User u : users) {
                        userModel.addRow(new Object[]{
                            u.getId(), u.getName(), u.getEmail(),
                            u.getRole().toUpperCase(),
                            u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0,10) : "—"
                        });
                    }
                    setStatus("Loaded " + users.size() + " users.");
                } catch (Exception ex) {
                    showError("Error loading users: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void syncStatuses() {
        try {
            auctionDAO.syncStatuses();
            loadAuctions();
            setStatus("Auction statuses synchronised.");
        } catch (SQLException ex) {
            showError("Sync error: " + ex.getMessage());
        }
    }

    // ── Set Winner Dialog ─────────────────────────────────────────────────────

    private void openSetWinnerDialog() {
        int row = auctionTable.getSelectedRow();
        if (row < 0) { showError("Select an auction first."); return; }

        int    auctionId = (int)    auctionModel.getValueAt(row, 0);
        String status    = (String) auctionModel.getValueAt(row, 6);

        if (!status.equals("ENDED")) {
            showError("Winner can only be set for ENDED auctions.");
            return;
        }

        try {
            List<Bid> bids = bidDAO.findByAuction(auctionId);
            if (bids.isEmpty()) { showError("No bids for this auction."); return; }

            // Build bidder options — unique bidders with highest bid
            java.util.LinkedHashMap<Integer, Bid> topBids = new java.util.LinkedHashMap<>();
            for (Bid b : bids) {
                if (!topBids.containsKey(b.getBidderId())) topBids.put(b.getBidderId(), b);
            }

            String[] options = topBids.values().stream()
                .map(b -> b.getBidderName() + " — ₹" + String.format("%,.2f", b.getBidAmount()))
                .toArray(String[]::new);

            String choice = (String) JOptionPane.showInputDialog(
                this, "Select winning bid for Auction #" + auctionId,
                "Set Winner", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == null) return;

            // Find the selected bid
            Bid[] bidArr = topBids.values().toArray(new Bid[0]);
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(choice)) {
                    Bid winner = bidArr[i];
                    auctionDAO.selectWinner(auctionId, winner.getBidderId(), winner.getBidAmount());
                    loadAuctions();
                    setStatus("Winner set: " + winner.getBidderName() +
                              " for auction #" + auctionId);
                    break;
                }
            }
        } catch (SQLException ex) {
            showError("DB error: " + ex.getMessage());
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT);
        table.setGridColor(new Color(255, 255, 255, 15));
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(28);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setBackground(BG_DARK);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setIntercellSpacing(new Dimension(10, 4));
        return table;
    }

    private void styleScroll(JScrollPane scroll) {
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.setBackground(BG_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 20)));
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void setStatus(String msg) {
        statusBar.setText("  " + msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Entry Point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AdminPanel panel = new AdminPanel();
            panel.setVisible(true);
        });
    }
}
