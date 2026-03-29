<%-- ============================================================
     buyer_dashboard.jsp — Buyer's personal bid activity dashboard
     Accessed via BuyerServlet → /buyer?action=dashboard
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, java.util.Map, java.util.LinkedHashMap,
                 model.Bid, model.Auction, model.User" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Bid> myBids = (List<Bid>) request.getAttribute("myBids");

    // Group bids by auctionId to compute stats
    int uniqueAuctions = 0;
    double totalSpent  = 0;
    int winningCount   = 0;

    if (myBids != null) {
        java.util.Set<Integer> auctionIds = new java.util.HashSet<>();
        for (Bid b : myBids) {
            auctionIds.add(b.getAuctionId());
            totalSpent += b.getBidAmount().doubleValue();
        }
        uniqueAuctions = auctionIds.size();
    }

    @SuppressWarnings("unchecked")
    List<Auction> wonAuctions = (List<Auction>) request.getAttribute("wonAuctions");
    winningCount = (wonAuctions != null) ? wonAuctions.size() : 0;

    request.setAttribute("pageTitle", "My Bids — AuctionLive");
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F9FE; My Bid Activity</h1>
    <p>Track your bids and auction wins, <strong><%= currentUser.getName() %></strong>.</p>
  </div>
</div>

<div class="container">

  <%-- Stats row --%>
  <div class="stats-row" style="margin-bottom:32px">
    <div class="stat-card">
      <div class="value"><%= myBids != null ? myBids.size() : 0 %></div>
      <div class="label">Total Bids Placed</div>
    </div>
    <div class="stat-card">
      <div class="value"><%= uniqueAuctions %></div>
      <div class="label">Auctions Entered</div>
    </div>
    <div class="stat-card">
      <div class="value" style="color:var(--gold)"><%= winningCount %></div>
      <div class="label">Auctions Won</div>
    </div>
    <div class="stat-card">
      <div class="value" style="font-size:1.3rem">
        &#8377;<%= String.format("%,.0f", totalSpent) %>
      </div>
      <div class="label">Total Bid Value</div>
    </div>
  </div>

  <%-- Won auctions --%>
  <% if (wonAuctions != null && !wonAuctions.isEmpty()) { %>
    <h2 style="margin-bottom:16px;font-size:1.2rem">&#x1F3C6; Auctions Won</h2>
    <div class="auction-grid" style="margin-bottom:40px">
      <% for (Auction a : wonAuctions) { %>
        <div class="card" style="border:1px solid rgba(240,165,0,.3)">
          <% if (a.getItem().getImagePath() != null && !a.getItem().getImagePath().isEmpty()) { %>
            <img class="card-img"
                 src="<%= request.getContextPath() %>/<%= a.getItem().getImagePath() %>"
                 alt="<%= a.getItem().getName() %>"
                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
            <div class="card-img-placeholder" style="display:none">&#x1F3C6;</div>
          <% } else { %>
            <div class="card-img-placeholder">&#x1F3C6;</div>
          <% } %>
          <div class="card-body">
            <div style="display:flex;justify-content:space-between;margin-bottom:8px">
              <h3 class="card-title" style="margin:0"><%= a.getItem().getName() %></h3>
              <span class="badge badge-active">WON</span>
            </div>
            <div class="price-tag" style="margin-bottom:12px">
              &#8377;<%= String.format("%,.2f", a.getWinningBid()) %>
            </div>
            <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= a.getId() %>"
               class="btn btn-gold btn-sm">View Auction</a>
          </div>
        </div>
      <% } %>
    </div>
  <% } %>

  <%-- All bids table --%>
  <h2 style="margin-bottom:16px;font-size:1.2rem">&#x1F4CA; All My Bids</h2>

  <% if (myBids == null || myBids.isEmpty()) { %>
    <div class="empty-state">
      <div class="icon">&#x1F4AC;</div>
      <h3>No bids yet</h3>
      <p>Browse active auctions and place your first bid!</p>
      <a href="<%= request.getContextPath() %>/auction?action=list"
         class="btn btn-gold" style="margin-top:16px">Browse Auctions &rarr;</a>
    </div>
  <% } else { %>
    <div class="table-wrap" style="margin-bottom:40px">
      <table>
        <thead>
          <tr>
            <th>Auction ID</th>
            <th>Your Bid</th>
            <th>Time Placed</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          <% for (Bid b : myBids) { %>
            <tr>
              <td>#<%= b.getAuctionId() %></td>
              <td>
                <span class="price-tag" style="font-size:.85rem">
                  &#8377;<%= String.format("%,.2f", b.getBidAmount()) %>
                </span>
              </td>
              <td class="muted" style="font-size:.88rem">
                <%= b.getBidTime().toString().substring(0,19).replace("T"," ") %>
              </td>
              <td>
                <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= b.getAuctionId() %>"
                   class="btn btn-outline btn-sm">View</a>
                &nbsp;
                <a href="<%= request.getContextPath() %>/bid?action=history&id=<%= b.getAuctionId() %>"
                   class="btn btn-sm" style="background:rgba(255,255,255,.06);color:var(--light)">History</a>
              </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>

    <div style="text-align:center;margin-bottom:40px">
      <a href="<%= request.getContextPath() %>/auction?action=list" class="btn btn-primary">
        &#x1F4E6; Browse More Auctions
      </a>
    </div>
  <% } %>

</div>

<%@ include file="footer.jsp" %>
