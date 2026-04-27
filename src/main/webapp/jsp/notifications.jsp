<%-- ============================================================
     notifications.jsp — list of win/loss notifications for the
     current user. Accessed via /notification?action=list
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, model.Notification, model.User" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");

    int unreadCount = 0;
    if (notifications != null) {
        for (Notification n : notifications) if (!n.isRead()) unreadCount++;
    }

    request.setAttribute("pageTitle", "Notifications — AuctionLive");
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F514; Notifications</h1>
    <p>Win and loss notices from auctions you bid on.</p>
  </div>
</div>

<div class="container" style="padding-top:30px">

  <% if (unreadCount > 0) { %>
    <form method="post" action="<%= request.getContextPath() %>/notification?action=markRead"
          style="margin-bottom:20px;text-align:right">
      <input type="hidden" name="action" value="markRead">
      <button type="submit" class="btn btn-outline btn-sm">
        &#x2714; Mark all <%= unreadCount %> as read
      </button>
    </form>
  <% } %>

  <% if (notifications == null || notifications.isEmpty()) { %>
    <div class="empty-state">
      <div class="icon">&#x1F4ED;</div>
      <h3>No notifications yet</h3>
      <p>You'll see win/loss notices here once auctions you bid on are decided.</p>
    </div>
  <% } else { %>
    <div style="display:flex;flex-direction:column;gap:12px;margin-bottom:40px">
      <% for (Notification n : notifications) {
           boolean win = n.isWin();
           String accent = win ? "#27ae60" : "#a0a0b0";
           String emoji  = win ? "&#x1F3C6;" : "&#x1F614;";
      %>
        <div style="display:flex;align-items:center;gap:16px;padding:18px 22px;
                    background:var(--surface);
                    border:1px solid <%= n.isRead() ? "rgba(255,255,255,.06)" : accent %>;
                    border-left:4px solid <%= accent %>;
                    border-radius:10px;
                    <%= n.isRead() ? "opacity:.75;" : "" %>">
          <div style="font-size:1.6rem"><%= emoji %></div>
          <div style="flex:1">
            <div style="font-weight:600;margin-bottom:4px;color:<%= accent %>">
              <%= n.getMessage() %>
              <% if (!n.isRead()) { %>
                <span style="font-size:.7rem;background:#e74c3c;color:#fff;padding:1px 8px;border-radius:10px;margin-left:8px;vertical-align:middle">NEW</span>
              <% } %>
            </div>
            <div class="muted" style="font-size:.88rem">
              Auction:
              <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= n.getAuctionId() %>"
                 style="color:var(--light)">
                <%= n.getAuctionItemName() != null ? n.getAuctionItemName() : ("#" + n.getAuctionId()) %>
              </a>
              &nbsp;&middot;&nbsp;
              <%= n.getCreatedAt().toString().substring(0,16).replace("T"," ") %>
            </div>
          </div>
        </div>
      <% } %>
    </div>
  <% } %>

</div>

<%@ include file="footer.jsp" %>
