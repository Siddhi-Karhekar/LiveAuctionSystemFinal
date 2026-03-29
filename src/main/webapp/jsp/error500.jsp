<%-- error500.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>500 — AuctionLive</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css"></head>
<body>
<div class="empty-state" style="min-height:80vh;display:flex;flex-direction:column;align-items:center;justify-content:center">
  <div class="icon" style="font-size:5rem">&#x26A0;</div>
  <h2>Internal Server Error</h2>
  <p class="muted">Something went wrong on our end. Please try again later.</p>
  <% if (exception != null) { %>
    <pre style="color:var(--danger);font-size:.8rem;margin-top:16px"><%= exception.getMessage() %></pre>
  <% } %>
  <a href="<%= request.getContextPath() %>/" class="btn btn-primary" style="margin-top:20px">Go Home</a>
</div>
</body></html>
