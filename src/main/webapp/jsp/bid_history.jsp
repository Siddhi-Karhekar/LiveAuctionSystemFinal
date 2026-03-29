<%-- ============================================================
     bid_history.jsp — Full bid history for an auction
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, model.Auction, model.Bid, model.User" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    Auction auction = (Auction) request.getAttribute("auction");
    @SuppressWarnings("unchecked")
    List<Bid> bids = (List<Bid>) request.getAttribute("bids");

    if (auction == null) {
        response.sendRedirect(request.getContextPath() + "/auction?action=list");
        return;
    }
    request.setAttribute("pageTitle", "Bid History — " + auction.getItem().getName());
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F4CA; Bid History</h1>
    <p><%= auction.getItem().getName() %>
       &nbsp;<span class="badge badge-<%= auction.getStatus() %>"><%= auction.getStatus() %></span>
    </p>
  </div>
</div>

<div class="container">

  <div style="margin-bottom:20px">
    <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= auction.getId() %>"
       style="color:var(--muted);font-size:.9rem">
      &larr; Back to Auction
    </a>
  </div>

  <%-- Summary card --%>
  <div style="display:flex;gap:16px;flex-wrap:wrap;margin-bottom:28px">
    <div class="stat-card" style="flex:1;min-width:150px">
      <div class="value"><%= bids != null ? bids.size() : 0 %></div>
      <div class="label">Total Bids</div>
    </div>
    <div class="stat-card" style="flex:1;min-width:150px">
      <div class="value" style="font-size:1.4rem">
        &#8377;<% if (auction.getCurrentHighestBid() != null) {
          %><%= String.format("%,.2f", auction.getCurrentHighestBid()) %><%
        } else { %>—<% } %>
      </div>
      <div class="label">Highest Bid</div>
    </div>
    <div class="stat-card" style="flex:1;min-width:150px">
      <div class="value" style="font-size:1.4rem">
        &#8377;<%= String.format("%,.2f", auction.getItem().getStartPrice()) %>
      </div>
      <div class="label">Starting Price</div>
    </div>
    <% if (auction.getWinnerId() != null) { %>
    <div class="stat-card" style="flex:1;min-width:150px">
      <div class="value" style="font-size:1rem;color:var(--gold)">
        &#x1F3C6; <%= auction.getWinnerName() %>
      </div>
      <div class="label">Winner</div>
    </div>
    <% } %>
  </div>

  <% if (bids == null || bids.isEmpty()) { %>
    <div class="empty-state">
      <div class="icon">&#x1F4AC;</div>
      <h3>No bids placed yet</h3>
      <p>Be the first to place a bid!</p>
      <% if (auction.isActive() && currentUser.isBuyer()) { %>
        <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= auction.getId() %>"
           class="btn btn-gold" style="margin-top:16px">Bid Now</a>
      <% } %>
    </div>
  <% } else { %>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Rank</th>
            <th>Bidder</th>
            <th>Bid Amount</th>
            <th>Time Placed</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <% int rank = 1; for (Bid b : bids) { %>
            <tr>
              <td>
                <% if (rank == 1) { %>
                  <span style="font-size:1.2rem">&#x1F947;</span>
                <% } else if (rank == 2) { %>
                  <span style="font-size:1.2rem">&#x1F948;</span>
                <% } else if (rank == 3) { %>
                  <span style="font-size:1.2rem">&#x1F949;</span>
                <% } else { %>
                  #<%= rank %>
                <% } %>
              </td>
              <td>
                <strong><%= b.getBidderName() %></strong>
                <% if (currentUser.getId() == b.getBidderId()) { %>
                  <span class="badge" style="background:rgba(233,69,96,.15);color:var(--accent);font-size:.7rem;margin-left:6px">You</span>
                <% } %>
              </td>
              <td>
                <span class="price-tag" style="<%= rank > 1 ? "background:none;border:none;color:var(--text);padding:0" : "" %>">
                  &#8377;<%= String.format("%,.2f", b.getBidAmount()) %>
                </span>
              </td>
              <td class="muted" style="font-size:.88rem">
                <%= b.getBidTime().toString().substring(0,19).replace("T"," ") %>
              </td>
              <td>
                <% if (auction.getWinnerId() != null && auction.getWinnerId() == b.getBidderId() && rank == 1) { %>
                  <span class="badge badge-active">Winner</span>
                <% } else if (rank == 1 && "ended".equals(auction.getStatus())) { %>
                  <span class="badge badge-scheduled">Highest</span>
                <% } else if (rank == 1) { %>
                  <span class="badge badge-active">Leading</span>
                <% } else { %>
                  <span class="badge badge-ended">Outbid</span>
                <% } %>
              </td>
            </tr>
          <% rank++; } %>
        </tbody>
      </table>
    </div>

    <% if (auction.isActive() && currentUser.isBuyer()) { %>
      <div style="text-align:center;margin-top:24px">
        <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= auction.getId() %>"
           class="btn btn-gold">Place a Bid &rarr;</a>
      </div>
    <% } %>

  <% } %>

</div>

<%@ include file="footer.jsp" %>
