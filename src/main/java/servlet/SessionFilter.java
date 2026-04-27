package servlet;

import model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * SessionFilter — guards all URLs except /auth and static resources.
 *
 * If a request arrives without a valid session, the user is redirected
 * to the login page. This removes the need for null-session checks in
 * every individual servlet.
 *
 * Protected:  /auction/*, /bid/*, /seller/*, /item/*
 * Public:     /auth/*, /css/*, /js/*, /uploads/*, *.jsp (login/register)
 */
@WebFilter(urlPatterns = {"/auction", "/bid", "/seller", "/item", "/buyer", "/notification"})
public class SessionFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        // Nothing to initialise
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User loggedUser     = (session != null) ? (User) session.getAttribute("loggedUser") : null;

        if (loggedUser == null) {
            // Not logged in — redirect to login, preserving requested URL
            String loginUrl = req.getContextPath() + "/auth?action=login&next="
                            + req.getRequestURI();
            resp.sendRedirect(loginUrl);
            return;
        }

        // Seller-only route guard
        String uri = req.getRequestURI();
        if ((uri.contains("/seller") || uri.contains("/item"))
                && !loggedUser.isSeller() && !loggedUser.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/auction?action=list");
            return;
        }

        // Passed all checks — continue to servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}
