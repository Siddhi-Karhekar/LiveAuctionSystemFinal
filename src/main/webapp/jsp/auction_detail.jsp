<%-- ============================================================
     auction_detail.jsp — Fixed: No diamond operator, no generics
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, java.util.LinkedHashMap, java.util.Collection,
                 model.Auction, model.Bid, model.User, java.math.BigDecimal" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    Auction auction = (Auction) request.getAttribute("auction");
    List bids = (List) request.getAttribute("bids");

    if (auction == null) {
        response.sendRedirect(request.getContextPath() + "/auction?action=list");
        return;
    }

    boolean isActive  = auction.isActive();
    boolean isEnded   = auction.isEnded();
    boolean isBuyer   = currentUser.isBuyer();
    boolean isSeller  = currentUser.isSeller() || currentUser.isAdmin();
    boolean isOwner   = isSeller && auction.getItem().getSellerId() == currentUser.getId();
    boolean hasWinner = auction.getWinnerId() != null;

    BigDecimal minBid = auction.getMinimumNextBid();

    String errorParam   = request.getParameter("error");
    String successParam = request.getParameter("success");
    String msgParam     = request.getParameter("msg");

    String endTimeISO = auction.getEndTime() != null
        ? auction.getEndTime().toInstant().toString() : "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><%= auction.getItem().getName() %> — AuctionLive</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<%@ include file="header.jsp" %>

<div class="container" style="padding-top:30px">

  <% if ("bid_too_low".equals(errorParam)) { %>
    <div class="alert alert-error">Your bid is too low. Minimum bid is &#8377;<%= String.format("%,.2f", minBid) %></div>
  <% } %>
  <% if ("not_active".equals(errorParam)) { %>
    <div class="alert alert-error">This auction is not currently accepting bids.</div>
  <% } %>
  <% if ("bid_placed".equals(successParam)) { %>
    <div class="alert alert-success">&#x2714; Your bid was placed successfully!</div>
  <% } %>
  <% if ("winner_set".equals(msgParam)) { %>
    <div class="alert alert-success">&#x1F3C6; Winner has been selected!</div>
  <% } %>

  <div style="margin-bottom:16px">
    <a href="<%= request.getContextPath() %>/auction?action=list" style="color:var(--muted);font-size:.9rem">&larr; Back to Auctions</a>
  </div>

  <div class="detail-layout">

    <div>
      <% if (auction.getItem().getImagePath() != null && !auction.getItem().getImagePath().isEmpty()) { %>
        <img class="detail-img"
             src="<%= request.getContextPath() %>/<%= auction.getItem().getImagePath() %>"
             alt="<%= auction.getItem().getName() %>"
             onerror="this.style.display='none'">
      <% } else { %>
        <div style="width:100%;height:300px;background:var(--card);border-radius:10px;display:flex;align-items:center;justify-content:center;font-size:5rem;margin-bottom:20px">&#x1F4E6;</div>
      <% } %>

      <div style="margin-top:24px">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
          <h1 style="font-size:1.8rem;margin:0"><%= auction.getItem().getName() %></h1>
          <span id="auctionStatusBadge" class="badge badge-<%= auction.getStatus() %>"><%= auction.getStatus() %></span>
        </div>
        <p class="muted" style="margin-bottom:6px">&#x1F464; Listed by <strong style="color:var(--light)"><%= auction.getItem().getSellerName() %></strong></p>
        <p class="muted" style="margin-bottom:20px">&#x1F4CB; Starting price: &#8377;<%= String.format("%,.2f", auction.getItem().getStartPrice()) %></p>

        <div style="background:var(--surface);border:1px solid rgba(255,255,255,.06);border-radius:10px;padding:22px;margin-bottom:24px">
          <h3 style="margin-bottom:12px;font-size:1rem;text-transform:uppercase;letter-spacing:.06em;color:var(--muted)">Description</h3>
          <p style="line-height:1.8"><%= auction.getItem().getDescription() %></p>
        </div>

        <div style="display:flex;gap:20px;flex-wrap:wrap">
          <div>
            <div class="muted" style="font-size:.8rem;margin-bottom:4px">START TIME</div>
            <div style="font-weight:600"><%= auction.getStartTime().toString().substring(0,16).replace("T"," ") %></div>
          </div>
          <div>
            <div class="muted" style="font-size:.8rem;margin-bottom:4px">END TIME</div>
            <div style="font-weight:600"><%= auction.getEndTime().toString().substring(0,16).replace("T"," ") %></div>
          </div>
        </div>
      </div>
    </div>

    <div>
      <div class="bid-box">
        <h3>&#x1F4B0; Current Bid</h3>
        <div class="current-bid" id="currentHighBid">
          &#8377;<% if (auction.getCurrentHighestBid() != null) { %><%= String.format("%,.2f", auction.getCurrentHighestBid()) %><% } else { %><%= String.format("%,.2f", auction.getItem().getStartPrice()) %><% } %>
        </div>
        <div class="muted" style="margin-bottom:16px;font-size:.9rem">
          <span id="totalBids"><%= auction.getTotalBids() %> bid<%= auction.getTotalBids() != 1 ? "s" : "" %></span>
        </div>

        <% if (isActive) { %>
          <div class="timer"><span class="live-dot"></span><span id="countdown">Loading...</span></div>
        <% } else if (auction.isScheduled()) { %>
          <div class="timer" style="color:var(--warning);background:rgba(243,156,18,.1);border-color:rgba(243,156,18,.25)">
            &#x23F0; Starts at <%= auction.getStartTime().toString().substring(0,16).replace("T"," ") %>
          </div>
        <% } else { %>
          <div class="timer" style="color:var(--muted);background:rgba(160,160,176,.1);border-color:rgba(160,160,176,.2)">&#x23F9; Auction Ended</div>
        <% } %>

        <% if (isEnded && hasWinner) { %>
          <div class="alert alert-success" style="margin-top:16px">
            &#x1F3C6; Winner: <strong><%= auction.getWinnerName() %></strong><br>
            Winning bid: &#8377;<%= String.format("%,.2f", auction.getWinningBid()) %>
          </div>
        <% } %>

        <% if (isActive && isBuyer) { %>
          <form method="post" action="<%= request.getContextPath() %>/bid?action=place"
                onsubmit="return validateBidForm(<%= minBid.toPlainString() %>)" style="margin-top:20px">
            <input type="hidden" name="auctionId" value="<%= auction.getId() %>">
            <div class="form-group">
              <label for="bidAmountInput">Your Bid (min &#8377;<%= String.format("%,.2f", minBid) %>)</label>
              <input type="number" id="bidAmountInput" name="bidAmount"
                     step="0.01" min="<%= minBid.toPlainString() %>"
                     placeholder="<%= minBid.toPlainString() %>" required>
            </div>
            <button type="submit" class="btn btn-gold btn-block">&#x1F4B3; Place Bid</button>
          </form>
        <% } else if (isActive && isSeller && !isOwner) { %>
          <div class="alert alert-info" style="margin-top:16px">Sellers cannot place bids.</div>
        <% } %>

        <% if (isEnded && !hasWinner && isOwner && (bids == null || bids.isEmpty())) { %>
          <div class="alert alert-info" style="margin-top:16px">No bids were placed on this auction.</div>
        <% } %>

        <div style="margin-top:16px;text-align:center">
          <a href="<%= request.getContextPath() %>/bid?action=history&id=<%= auction.getId() %>"
             style="font-size:.85rem;color:var(--muted)">View Full Bid History &#x2197;</a>
        </div>
      </div>
    </div>
  </div>

  <%-- ── Seller's manual winner-selection panel ─────────────────────────── --%>
  <% if (isEnded && !hasWinner && isOwner && bids != null && !bids.isEmpty()) {
       LinkedHashMap topBids = new LinkedHashMap();
       for (int wi = 0; wi < bids.size(); wi++) {
           Bid wb = (Bid) bids.get(wi);
           Integer bidderKey = new Integer(wb.getBidderId());
           if (!topBids.containsKey(bidderKey)) {
               topBids.put(bidderKey, wb);   // bids are pre-sorted by amount DESC, so first seen = highest
           }
       }
       Bid[] topBidArr = (Bid[]) topBids.values().toArray(new Bid[0]);
  %>
    <div style="margin-bottom:40px;padding:24px;background:var(--surface);border:1px solid rgba(240,165,0,.3);border-radius:10px">
      <h2 style="margin-bottom:8px;font-size:1.3rem">&#x1F3C6; Select the Winning Bidder</h2>
      <p class="muted" style="margin-bottom:20px;font-size:.92rem">
        The auction has ended. Review the bidders below and choose the winner.
        The winner will be notified <strong>"You won the bid!"</strong> and the others
        will receive <strong>"You did not win the bid"</strong>.
      </p>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Bidder</th><th>Highest Bid</th><th>Action</th></tr>
          </thead>
          <tbody>
            <% for (int wi2 = 0; wi2 < topBidArr.length; wi2++) {
                 Bid wb2 = topBidArr[wi2];
            %>
              <tr>
                <td><% if (wi2 == 0) { %><span style="color:var(--gold)">&#x1F947;</span><% } else { %><%= wi2 + 1 %><% } %></td>
                <td><strong><%= wb2.getBidderName() %></strong></td>
                <td><span class="price-tag">&#8377;<%= String.format("%,.2f", wb2.getBidAmount()) %></span></td>
                <td>
                  <form method="post" action="<%= request.getContextPath() %>/auction?action=selectWinner"
                        style="margin:0"
                        onsubmit="return confirmAction('Select <%= wb2.getBidderName() %> as the winner of this auction?');">
                    <input type="hidden" name="auctionId" value="<%= auction.getId() %>">
                    <input type="hidden" name="bidderId"  value="<%= wb2.getBidderId() %>">
                    <input type="hidden" name="bidAmount" value="<%= wb2.getBidAmount().toPlainString() %>">
                    <button type="submit" class="btn btn-gold btn-sm">&#x1F3C6; Select as Winner</button>
                  </form>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </div>
  <% } %>

  <% if (bids != null && !bids.isEmpty()) { %>
    <div style="margin-bottom:40px">
      <h2 style="margin-bottom:20px;font-size:1.3rem">&#x1F4CA; Bid Activity</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Bidder</th><th>Amount</th><th>Time</th></tr>
          </thead>
          <tbody>
            <%
              int rank = 1;
              for (int bi = 0; bi < bids.size(); bi++) {
                Bid b = (Bid) bids.get(bi);
                if (rank > 10) break;
            %>
              <tr>
                <td><% if (rank == 1) { %><span style="color:var(--gold)">&#x1F947;</span><% } else { %><%= rank %><% } %></td>
                <td><strong><%= b.getBidderName() %></strong></td>
                <td><span class="price-tag" style="<%= rank > 1 ? "background:none;border:none;color:var(--text)" : "" %>">&#8377;<%= String.format("%,.2f", b.getBidAmount()) %></span></td>
                <td class="muted"><%= b.getBidTime().toString().substring(0,16).replace("T"," ") %></td>
              </tr>
            <% rank++; } %>
          </tbody>
        </table>
      </div>
      <% if (bids.size() > 10) { %>
        <div style="text-align:center;margin-top:12px">
          <a href="<%= request.getContextPath() %>/bid?action=history&id=<%= auction.getId() %>">Show all <%= bids.size() %> bids &rarr;</a>
        </div>
      <% } %>
    </div>
  <% } %>

</div>

<script>
  <% if (isActive) { %>
    startCountdown('<%= endTimeISO %>');
    startBidPolling(<%= auction.getId() %>, '<%= request.getContextPath() %>');
  <% } %>
</script>

<%@ include file="footer.jsp" %>
