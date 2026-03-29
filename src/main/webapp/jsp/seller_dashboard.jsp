<%-- ============================================================
     seller_dashboard.jsp — Seller's auction management dashboard
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, model.Auction, model.User" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null || (!currentUser.isSeller() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Auction> auctions = (List<Auction>) request.getAttribute("auctions");
    String successParam = request.getParameter("success");

    // Compute quick stats
    int totalAuctions = auctions != null ? auctions.size() : 0;
    int activeCount   = 0, endedCount = 0, scheduledCount = 0;
    if (auctions != null) {
        for (Auction a : auctions) {
            if ("active".equals(a.getStatus())) activeCount++;
            else if ("ended".equals(a.getStatus())) endedCount++;
            else if ("scheduled".equals(a.getStatus())) scheduledCount++;
        }
    }
    request.setAttribute("pageTitle", "Dashboard — AuctionLive");
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F4CA; Seller Dashboard</h1>
    <p>Welcome back, <strong><%= currentUser.getName() %></strong>
       &nbsp;<span class="badge badge-active"><%= currentUser.getRole() %></span>
    </p>
  </div>
</div>

<div class="container">

  <% if ("1".equals(successParam)) { %>
    <div class="alert alert-success">&#x2714; Your auction has been listed successfully!</div>
  <% } %>

  <%-- Stats row --%>
  <div class="stats-row">
    <div class="stat-card">
      <div class="value"><%= totalAuctions %></div>
      <div class="label">Total Auctions</div>
    </div>
    <div class="stat-card">
      <div class="value" style="color:var(--success)"><%= activeCount %></div>
      <div class="label">Active Now</div>
    </div>
    <div class="stat-card">
      <div class="value" style="color:var(--warning)"><%= scheduledCount %></div>
      <div class="label">Scheduled</div>
    </div>
    <div class="stat-card">
      <div class="value" style="color:var(--muted)"><%= endedCount %></div>
      <div class="label">Ended</div>
    </div>
  </div>

  <%-- CTA --%>
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
    <h2 style="font-size:1.3rem">Your Auctions</h2>
    <a href="<%= request.getContextPath() %>/item?action=form" class="btn btn-primary">
      + List New Item
    </a>
  </div>

  <% if (auctions == null || auctions.isEmpty()) { %>
    <div class="empty-state">
      <div class="icon">&#x1F4AD;</div>
      <h3>No auctions yet</h3>
      <p>Get started by listing your first item.</p>
      <a href="<%= request.getContextPath() %>/item?action=form" class="btn btn-gold" style="margin-top:16px">
        + List an Item
      </a>
    </div>
  <% } else { %>

    <div class="table-wrap" style="margin-bottom:40px">
      <table>
        <thead>
          <tr>
            <th>Item</th>
            <th>Start Price</th>
            <th>Highest Bid</th>
            <th>Bids</th>
            <th>Status</th>
            <th>Ends</th>
            <th>Winner</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <% for (Auction a : auctions) { %>
            <tr>
              <td><strong><%= a.getItem().getName() %></strong></td>
              <td>&#8377;<%= String.format("%,.2f", a.getItem().getStartPrice()) %></td>
              <td>
                <% if (a.getCurrentHighestBid() != null) { %>
                  <span class="price-tag" style="font-size:.85rem">
                    &#8377;<%= String.format("%,.2f", a.getCurrentHighestBid()) %>
                  </span>
                <% } else { %>
                  <span class="muted">—</span>
                <% } %>
              </td>
              <td><%= a.getTotalBids() %></td>
              <td><span class="badge badge-<%= a.getStatus() %>"><%= a.getStatus() %></span></td>
              <td class="muted" style="font-size:.85rem">
                <%= a.getEndTime() != null ? a.getEndTime().toString().substring(0,16).replace("T"," ") : "—" %>
              </td>
              <td>
                <% if (a.getWinnerId() != null) { %>
                  <span style="color:var(--gold)">&#x1F3C6; <%= a.getWinnerName() %></span>
                <% } else if ("ended".equals(a.getStatus()) && a.getTotalBids() > 0) { %>
                  <span style="color:var(--warning);font-size:.85rem">Pending selection</span>
                <% } else { %>
                  <span class="muted">—</span>
                <% } %>
              </td>
              <td>
                <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= a.getId() %>"
                   class="btn btn-outline btn-sm">View</a>
                &nbsp;
                <a href="<%= request.getContextPath() %>/bid?action=history&id=<%= a.getId() %>"
                   class="btn btn-sm" style="background:rgba(255,255,255,.06);color:var(--light)">Bids</a>
              </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>

  <% } %>
</div>

<%@ include file="footer.jsp" %>
