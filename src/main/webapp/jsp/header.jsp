<%-- ============================================================
     header.jsp — shared navbar included in every page
     ============================================================ --%>
<%@ page import="model.User" %>
<%
    User navUser = (User) session.getAttribute("loggedUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Live Auction System" %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<nav>
  <div class="container nav-inner">
    <a class="nav-brand" href="<%= request.getContextPath() %>/auction?action=list">
      &#x1F528; Auction<span>Live</span>
    </a>

    <div class="nav-links">
      <% if (navUser != null) { %>
        <a href="<%= request.getContextPath() %>/auction?action=list">Browse</a>

        <% if (navUser.isSeller() || navUser.isAdmin()) { %>
          <a href="<%= request.getContextPath() %>/seller?action=dashboard">Dashboard</a>
          <a href="<%= request.getContextPath() %>/item?action=form">+ List Item</a>
        <% } else if (navUser.isBuyer()) { %>
          <a href="<%= request.getContextPath() %>/buyer?action=dashboard">My Bids</a>
        <% } %>

        <span class="muted" style="font-size:.85rem">Hi, <%= navUser.getName().split(" ")[0] %></span>
        <a href="<%= request.getContextPath() %>/auth?action=logout" class="btn-nav">Logout</a>

      <% } else { %>
        <a href="<%= request.getContextPath() %>/auth?action=login">Login</a>
        <a href="<%= request.getContextPath() %>/auth?action=register" class="btn-nav">Register</a>
      <% } %>
    </div>
  </div>
</nav>
