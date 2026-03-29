<%-- ============================================================
     login.jsp — User login page
     ============================================================ --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User" %>
<%
    // If already logged in, redirect
    User existing = (session != null) ? (User) session.getAttribute("loggedUser") : null;
    if (existing != null) {
        if (existing.isSeller()) response.sendRedirect(request.getContextPath() + "/seller?action=dashboard");
        else response.sendRedirect(request.getContextPath() + "/auction?action=list");
        return;
    }

    String error   = (String) request.getAttribute("error");
    String success = (String) request.getAttribute("success");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login — AuctionLive</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<div class="auth-wrap">
  <div class="auth-box">
    <div class="auth-brand">&#x1F528; Auction<span>Live</span></div>
    <h2 style="text-align:center;margin-bottom:24px;font-size:1.4rem">Sign In</h2>

    <% if (error != null) { %>
      <div class="alert alert-error"><%= error %></div>
    <% } %>
    <% if (success != null) { %>
      <div class="alert alert-success"><%= success %></div>
    <% } %>

    <form method="post" action="<%= request.getContextPath() %>/auth?action=login">
      <div class="form-group">
        <label for="email">Email Address</label>
        <input type="email" id="email" name="email" placeholder="you@example.com" required autofocus>
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input type="password" id="password" name="password" placeholder="••••••••" required>
      </div>

      <button type="submit" class="btn btn-primary btn-block" style="margin-top:8px">
        Sign In &rarr;
      </button>
    </form>

    <div class="auth-divider" style="margin-top:24px">
      Don't have an account?
      <a href="<%= request.getContextPath() %>/auth?action=register">Create one</a>
    </div>

    <%-- Demo credentials hint --%>
    <div style="margin-top:28px;padding:14px;background:rgba(255,255,255,0.04);border-radius:8px;font-size:0.82rem;color:var(--muted)">
      <strong style="color:var(--light)">Demo Accounts:</strong><br>
      Seller: alice@auction.com / password123<br>
      Buyer &nbsp;: bob@auction.com &nbsp;&nbsp;/ password123<br>
      Admin &nbsp;: admin@auction.com / password123
    </div>
  </div>
</div>

<script src="<%= request.getContextPath() %>/js/main.js"></script>
</body>
</html>
