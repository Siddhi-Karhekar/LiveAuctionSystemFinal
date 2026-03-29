<%-- index.jsp — Root redirect based on login state --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.User" %>
<%
    User user = (session != null) ? (User) session.getAttribute("loggedUser") : null;
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    } else if (user.isSeller() || user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/seller?action=dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/auction?action=list");
    }
%>
