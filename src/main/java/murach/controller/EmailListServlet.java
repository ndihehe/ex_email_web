package murach.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.model.User;
import murach.service.UserService;
import murach.util.MailUtil;

@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {

    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String url = "/index.jsp";

        String action = request.getParameter("action");
        if (action == null) {
            action = "join";
        }

        if (action.equals("join")) {
            url = "/index.jsp";
        }
        else if (action.equals("add")) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String Email = request.getParameter("Email");

            User user = new User(firstName, lastName, Email);

            String errorMessage = userService.registerUser(user);

            if (errorMessage != null) {
                url = "/index.jsp";
                request.setAttribute("message", errorMessage);
            } else {
                url = "/thanks.jsp";
                request.setAttribute("message", "");

                // Gửi email cảm ơn bằng định dạng HTML tới người dùng
                String to = user.getEmail();
                String from = MailUtil.getSenderEmail();
                if (from == null || from.trim().isEmpty()) {
                    from = "no-reply@murach.com";
                }
                String subject = "Welcome to our email list";
                String body = MailUtil.buildWelcomeEmail(user);
                boolean isBodyHTML = true;

                try {
                    MailUtil.sendMail(to, from, subject, body, isBodyHTML);
                    request.setAttribute("emailStatus", "Một email xác nhận đã được gửi đến " + to);
                } catch (Exception e) {
                    this.log("Lỗi khi gửi email: " + e.getMessage());
                    request.setAttribute("emailStatus", "Lưu ý: Không thể gửi email xác nhận. Chi tiết: " + e.getMessage());
                }
            }

            request.setAttribute("user", user);
        }

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
