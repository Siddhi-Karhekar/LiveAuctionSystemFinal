<%-- ============================================================
     register.jsp — New user registration
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Register — AuctionLive</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<div class="auth-wrap">
  <div class="auth-box">
    <div class="auth-brand">&#x1F528; Auction<span>Live</span></div>
    <h2 style="text-align:center;margin-bottom:24px;font-size:1.4rem">Create Account</h2>

    <% if (error != null) { %>
      <div class="alert alert-error"><%= error %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/auth?action=register">

      <div class="form-group">
        <label for="name">Full Name</label>
        <input type="text" id="name" name="name" placeholder="John Doe" required>
      </div>

      <div class="form-group">
        <label for="email">Email Address</label>
        <input type="email" id="email" name="email" placeholder="you@example.com" required>
      </div>

      <div class="form-group">
        <label for="password">Password <span class="muted" style="font-weight:400">(min 6 chars)</span></label>
        <input type="password" id="password" name="password" placeholder="••••••••" minlength="6" required>
      </div>

      <div class="form-group">
        <label for="role">I want to</label>
        <select id="role" name="role" required>
          <option value="">— Select role —</option>
          <option value="buyer">Buy items (Buyer)</option>
          <option value="seller">Sell items (Seller)</option>
        </select>
      </div>

      <button type="submit" class="btn btn-primary btn-block" style="margin-top:8px">
        Create Account &rarr;
      </button>
    </form>

    <div class="auth-divider" style="margin-top:24px">
      Already have an account?
      <a href="<%= request.getContextPath() %>/auth?action=login">Sign in</a>
    </div>
  </div>
</div>

<script src="<%= request.getContextPath() %>/js/main.js"></script>
</body>
</html>
