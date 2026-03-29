<%-- ============================================================
     auction_list.jsp — Active auction browsing page (buyers)
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, model.Auction, model.User, java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    @SuppressWarnings("unchecked")
    List<Auction> auctions = (List<Auction>) request.getAttribute("auctions");
    String errorParam = request.getParameter("error");
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F4E6; Live Auctions</h1>
    <p>Bid on items before the clock runs out!</p>
  </div>
</div>

<div class="container">

  <% if ("sellers_cannot_bid".equals(errorParam)) { %>
    <div class="alert alert-error">Sellers cannot place bids. Please use a buyer account.</div>
  <% } %>

  <% if (auctions == null || auctions.isEmpty()) { %>
    <div class="empty-state">
      <div class="icon">&#x1F4AD;</div>
      <h3>No active auctions right now</h3>
      <p>Check back soon — new items go live regularly.</p>
    </div>
  <% } else { %>

    <div style="margin-bottom:16px;color:var(--muted);font-size:.9rem">
      Showing <strong style="color:var(--text)"><%= auctions.size() %></strong> active auction<%= auctions.size() != 1 ? "s" : "" %>
    </div>

    <div class="auction-grid">
      <% for (Auction a : auctions) { %>
        <div class="card">
          <%-- Item image --%>
          <% if (a.getItem().getImagePath() != null && !a.getItem().getImagePath().isEmpty()) { %>
            <img class="card-img"
                 src="<%= request.getContextPath() %>/<%= a.getItem().getImagePath() %>"
                 alt="<%= a.getItem().getName() %>"
                 onerror="this.style.display='none';this.nextElementSibling.style.display='flex'">
            <div class="card-img-placeholder" style="display:none">&#x1F4E6;</div>
          <% } else { %>
            <div class="card-img-placeholder">&#x1F4E6;</div>
          <% } %>

          <div class="card-body">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px">
              <h3 class="card-title" style="margin:0;flex:1"><%= a.getItem().getName() %></h3>
              <span class="badge badge-<%= a.getStatus() %>"><%= a.getStatus() %></span>
            </div>

            <p class="card-meta" style="margin-bottom:12px">
              Seller: <%= a.getItem().getSellerName() %>
            </p>

            <%-- Current highest bid --%>
            <div style="margin-bottom:12px">
              <div class="price-start">Current Bid</div>
              <div class="price-tag">
                &#8377;
                <% if (a.getCurrentHighestBid() != null) { %>
                  <%= String.format("%,.2f", a.getCurrentHighestBid()) %>
                <% } else { %>
                  <%= String.format("%,.2f", a.getItem().getStartPrice()) %> <span style="font-size:.75rem;opacity:.7">(start)</span>
                <% } %>
              </div>
            </div>

            <%-- Bid count --%>
            <div class="muted" style="font-size:.84rem;margin-bottom:12px">
              &#x1F4CA; <%= a.getTotalBids() %> bid<%= a.getTotalBids() != 1 ? "s" : "" %> &nbsp;|&nbsp;
              Ends: <%= a.getEndTime() != null ? a.getEndTime().toString().substring(0, 16) : "—" %>
            </div>

            <a href="<%= request.getContextPath() %>/auction?action=detail&id=<%= a.getId() %>"
               class="btn btn-gold btn-block" style="text-align:center">
              View &amp; Bid &rarr;
            </a>
          </div>
        </div>
      <% } %>
    </div>

  <% } %>
</div>

<%@ include file="footer.jsp" %>
