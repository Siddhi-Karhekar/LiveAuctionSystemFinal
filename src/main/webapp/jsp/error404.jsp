<%-- error404.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>404 — AuctionLive</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css"></head>
<body>
<div class="empty-state" style="min-height:80vh;display:flex;flex-direction:column;align-items:center;justify-content:center">
  <div class="icon" style="font-size:5rem">&#x1F50D;</div>
  <h2>Page Not Found</h2>
  <p class="muted">The page you're looking for doesn't exist.</p>
  <a href="<%= request.getContextPath() %>/" class="btn btn-primary" style="margin-top:20px">Go Home</a>
</div>
</body></html>
