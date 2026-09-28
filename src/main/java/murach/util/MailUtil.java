package murach.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {

    public static String getSenderEmail() {
        return System.getenv("MAIL_USERNAME");
    }

    public static String getAppPassword() {
        return System.getenv("MAIL_PASSWORD");
    }

    public static void sendMail(String to, String from,
                                String subject, String body, 
                                boolean bodyIsHTML) 
            throws MessagingException, UnsupportedEncodingException {

        final String username = getSenderEmail();
        final String password = getAppPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new MessagingException("Chưa cấu hình biến môi trường MAIL_USERNAME hoặc MAIL_PASSWORD trên máy chủ!");
        }

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        

        session.setDebug(true);


        Message message = new MimeMessage(session);
        message.setSubject(subject);

        if (bodyIsHTML) {
            message.setContent(body, "text/html; charset=UTF-8");
        } else {
            message.setText(body);
        }


        Address fromAddress = new InternetAddress(from, "Hệ Thống Web");
        Address toAddress = new InternetAddress(to);
        message.setFrom(fromAddress);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        Transport.send(message);
    }

    public static String buildWelcomeEmail(murach.model.User user) {
        return "<div style='background-color: #0d1117; padding: 35px 15px; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
                + "  <div style='max-width: 580px; margin: 0 auto; background-color: #161b22; border-radius: 14px; border: 1px solid #30363d; overflow: hidden; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);'>"
                + "    <div style='background-color: #0d1117; padding: 25px 20px; text-align: center; border-bottom: 2px solid #00f3ff;'>"
                + "      <h1 style='margin: 0; font-size: 24px; font-weight: 800; color: #00f3ff; text-shadow: 0 0 10px #00f3ff, 0 0 20px #00f3ff, 0 0 30px #00b4d8; text-transform: uppercase; letter-spacing: 1.5px;'>Welcome to our email list</h1>"
                + "    </div>"
                + "    <div style='padding: 30px; color: #e6edf3; line-height: 1.8; font-size: 15px;'>"
                + "      <p style='margin-top: 0; font-size: 17px;'>Dear <b style='color: #00f3ff; font-weight: bold; text-shadow: 0 0 5px rgba(0, 243, 255, 0.4);'>" + user.getFirstName() + "</b>,</p>"
                + "      <p>Thanks for joining our email list. We'll make sure to send you announcements about new products and promotions.</p>"
                + "      <p>Have a great day and thanks again!</p>"
                + "      <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #30363d; color: #8b949e; font-size: 14px;'>"
                + "        <p style='margin: 0; font-size: 16px; font-weight: bold; color: #00f3ff; text-shadow: 0 0 6px rgba(0, 243, 255, 0.3);'>Nhat Duy</p>"
                + "        <p style='margin: 3px 0 0 0; color: #8b949e;'> &amp;</p>"
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</div>";
    }
}
