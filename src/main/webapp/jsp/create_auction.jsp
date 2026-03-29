<%-- ============================================================
     create_auction.jsp — Seller form to list an item for auction
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User" %>
<%
    User currentUser = (User) session.getAttribute("loggedUser");
    if (currentUser == null || (!currentUser.isSeller() && !currentUser.isAdmin())) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    String error = (String) request.getAttribute("error");

    // Default start: now+1h, end: now+25h (convenient defaults)
    java.time.LocalDateTime defaultStart = java.time.LocalDateTime.now()
        .plusHours(1).withSecond(0).withNano(0);
    java.time.LocalDateTime defaultEnd = defaultStart.plusHours(24);
    String startDefault = defaultStart.toString().substring(0,16);
    String endDefault   = defaultEnd.toString().substring(0,16);

    request.setAttribute("pageTitle", "List Item — AuctionLive");
%>
<%@ include file="header.jsp" %>

<div class="page-header">
  <div class="container">
    <h1>&#x1F4E6; List a New Item</h1>
    <p>Fill in the details below to create an auction listing.</p>
  </div>
</div>

<div class="container">
  <div style="max-width:680px;margin:0 auto">

    <% if (error != null) { %>
      <div class="alert alert-error"><%= error %></div>
    <% } %>

    <form method="post"
          action="<%= request.getContextPath() %>/item?action=create"
          enctype="multipart/form-data"
          style="background:var(--surface);border:1px solid rgba(255,255,255,.06);
                 border-radius:10px;padding:36px;box-shadow:var(--shadow)">

      <h2 style="margin-bottom:28px;font-size:1.3rem">&#x1F6CD; Item Details</h2>

      <div class="form-group">
        <label for="itemName">Item Name *</label>
        <input type="text" id="itemName" name="itemName"
               placeholder="e.g. Vintage Rolex Submariner" maxlength="200" required>
      </div>

      <div class="form-group">
        <label for="description">Description *</label>
        <textarea id="description" name="description" rows="4"
                  placeholder="Describe the item condition, history, features..." required></textarea>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
        <div class="form-group">
          <label for="startPrice">Starting Price (&#8377;) *</label>
          <input type="number" id="startPrice" name="startPrice"
                 step="0.01" min="1" placeholder="500.00" required>
        </div>

        <div class="form-group">
          <label for="itemImage">Item Image (max 5MB)</label>
          <input type="file" id="itemImage" name="itemImage"
                 accept="image/jpeg,image/png,image/gif,image/webp"
                 onchange="previewImage(this, document.getElementById('imgPreview'))">
        </div>
      </div>

      <%-- Image preview --%>
      <div style="margin-bottom:20px">
        <img id="imgPreview" src="#" alt="Preview"
             style="display:none;max-width:100%;max-height:200px;
                    border-radius:8px;border:1px solid rgba(255,255,255,.1)">
      </div>

      <hr style="border:none;border-top:1px solid rgba(255,255,255,.06);margin:8px 0 24px">

      <h2 style="margin-bottom:20px;font-size:1.3rem">&#x23F0; Auction Schedule</h2>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
        <div class="form-group">
          <label for="startTime">Auction Start *</label>
          <input type="datetime-local" id="startTime" name="startTime"
                 value="<%= startDefault %>" required>
        </div>

        <div class="form-group">
          <label for="endTime">Auction End *</label>
          <input type="datetime-local" id="endTime" name="endTime"
                 value="<%= endDefault %>" required>
        </div>
      </div>

      <div class="alert alert-info" style="margin-bottom:20px">
        &#x2139; Bids can only be placed between start and end time.
        You can select the winning bidder after the auction ends.
      </div>

      <div style="display:flex;gap:12px">
        <button type="submit" class="btn btn-gold" style="flex:1">
          &#x1F680; Create Auction
        </button>
        <a href="<%= request.getContextPath() %>/seller?action=dashboard"
           class="btn btn-outline" style="flex:0 0 auto">Cancel</a>
      </div>

    </form>

  </div>
</div>

<%-- Validate end > start before submit --%>
<script>
document.querySelector('form').addEventListener('submit', function(e) {
    var start = new Date(document.getElementById('startTime').value);
    var end   = new Date(document.getElementById('endTime').value);
    if (end <= start) {
        e.preventDefault();
        alert('Auction end time must be after start time.');
    }
});
</script>

<%@ include file="footer.jsp" %>
