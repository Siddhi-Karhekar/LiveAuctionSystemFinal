package servlet;

import dao.ItemDAO;
import model.Item;
import model.User;
import util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * ItemServlet - seller creates/views items (with image upload).
 *
 * URL mappings:
 *   GET  /item?action=form         → create_auction.jsp (item + auction form)
 *   POST /item?action=create       → save item (delegates auction to AuctionServlet)
 */
@WebServlet("/item")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB in-memory threshold
    maxFileSize       = 5 * 1024 * 1024,   // 5 MB per file
    maxRequestSize    = 10 * 1024 * 1024   // 10 MB total request
)
public class ItemServlet extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();

    /** Directory (relative to webapp root) where uploads are stored. */
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null || !user.isSeller()) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }
        req.getRequestDispatcher("/jsp/create_auction.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getSessionUser(req);
        if (user == null || !user.isSeller()) {
            resp.sendRedirect(req.getContextPath() + "/auth?action=login");
            return;
        }

        String name        = req.getParameter("itemName");
        String description = req.getParameter("description");
        String priceStr    = req.getParameter("startPrice");
        String startTime   = req.getParameter("startTime");
        String endTime     = req.getParameter("endTime");

        // Validate text fields
        if (!ValidationUtil.isNotBlank(name) || !ValidationUtil.isNotBlank(description)
                || !ValidationUtil.isPositiveDouble(priceStr)
                || !ValidationUtil.isNotBlank(startTime)
                || !ValidationUtil.isNotBlank(endTime)) {
            req.setAttribute("error", "All fields are required and price must be positive.");
            req.getRequestDispatcher("/jsp/create_auction.jsp").forward(req, resp);
            return;
        }

        // Handle image upload
        String imagePath = null;
        Part filePart = req.getPart("itemImage");
        if (filePart != null && filePart.getSize() > 0) {
            String fileName = extractFileName(filePart);
            if (fileName != null && !fileName.isEmpty()) {
                // Determine absolute upload directory (inside webapp)
                String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                // Use timestamp prefix to avoid name collisions
                String safeFileName = System.currentTimeMillis() + "_" + fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                String fullPath = uploadPath + File.separator + safeFileName;
                filePart.write(fullPath);
                imagePath = UPLOAD_DIR + "/" + safeFileName;
            }
        }

        try {
            // 1. Save the item
            Item item = new Item();
            item.setSellerId(user.getId());
            item.setName(name.trim());
            item.setDescription(description.trim());
            item.setStartPrice(new BigDecimal(priceStr));
            item.setImagePath(imagePath);
            int itemId = itemDAO.insert(item);

            // 2. Forward item ID + times to AuctionServlet for auction creation
            req.setAttribute("itemId",    itemId);
            req.setAttribute("startTime", startTime);
            req.setAttribute("endTime",   endTime);
            req.getRequestDispatcher("/auction").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error saving item", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Extract original file name from a multipart Part. */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;
        for (String token : contentDisp.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private User getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("loggedUser") : null;
    }
}
