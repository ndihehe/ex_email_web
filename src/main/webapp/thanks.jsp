<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="murach.business.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Murach's Java Servlets and JSP</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
</head>
<body>
<%
    User user = (User) request.getAttribute("user");
%>
    <h1>Thank you!</h1>
    <p>
        <%= user.getFirstName() %> <%= user.getLastName() %>,
        thank you for joining our email list.
        A confirmation has been sent to <%= user.getEmail() %>.
    </p>
</body>
</html>