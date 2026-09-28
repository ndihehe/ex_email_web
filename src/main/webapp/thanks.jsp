<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Murach's Java Servlets and JSP</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
</head>
<body>
    <h1>Thanks for joining our email list</h1>

    <p>Here is the information that you entered:</p>

    <label>Email:</label>
    <span>${user.email}</span><br>

    <label>First Name:</label>
    <span>${user.firstName}</span><br>

    <label>Last Name:</label>
    <span>${user.lastName}</span><br>

    <p style="color: green; font-weight: bold; font-style: italic;">${emailStatus}</p>

    <p>To enter another email address, click on the Return button below.</p>

    <form action="emailList" method="post">
        <input type="hidden" name="action" value="join">
        <input type="submit" value="Return" id="submit">
    </form>
</body>
</html>
