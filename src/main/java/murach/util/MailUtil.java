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

        // Ép Java luôn sử dụng IPv4 để kết nối Gmail (tránh timeout do mạng không định tuyến được IPv6)
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
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


        Address fromAddress = new InternetAddress(from, "ndihehe");
        Address toAddress = new InternetAddress(to);
        message.setFrom(fromAddress);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        Transport.send(message);
    }

    private static String buildRainbowTitle(String text) {
        String[] colors = {
            "#ff0055", "#ff5500", "#ffaa00", "#ffee00", 
            "#00ff66", "#00f3ff", "#0088ff", "#7928ca", 
            "#b800ff", "#ff007f"
        };
        StringBuilder sb = new StringBuilder();
        int colorIdx = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                sb.append("<span style='display:inline-block; width:10px;'>&nbsp;</span>");
            } else {
                String color = colors[colorIdx % colors.length];
                colorIdx++;
                sb.append("<span style='color: ").append(color)
                  .append("; text-shadow: 0 0 10px ").append(color)
                  .append(", 0 0 20px ").append(color)
                  .append("; font-weight: 900;'>")
                  .append(c)
                  .append("</span>");
            }
        }
        return sb.toString();
    }

    public static String buildWelcomeEmail(murach.model.User user) {
        String rainbowTitle = buildRainbowTitle("WELCOME TO OUR EMAIL LIST");

        return "<div style='background-color: #0b0e14; padding: 35px 15px; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
                + "  <div style='max-width: 580px; margin: 0 auto; background-color: #151922; border-radius: 16px; border: 1px solid #2d3342; overflow: hidden; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7);'>"
                + "    <div style='background-color: #0b0e14; padding: 25px 20px; text-align: center; border-bottom: 2px solid #5865f2;'>"
                + "      <h1 style='margin: 0; font-size: 24px; font-weight: 900; letter-spacing: 2px; text-transform: uppercase;'>"
                +          rainbowTitle
                + "      </h1>"
                + "    </div>"
                + "    <div style='padding: 30px; color: #e6edf3; line-height: 1.8; font-size: 15px;'>"
                + "      <p style='margin-top: 0; font-size: 17px;'>Dear <b style='color: #00f3ff; font-weight: bold; text-shadow: 0 0 5px rgba(0, 243, 255, 0.4);'>" + user.getFirstName() + "</b>,</p>"
                + "      <p>Thanks for joining our email list. We'll make sure to send you announcements about new products and promotions.</p>"
                + "      <p>Have a great day and thanks again!</p>"
                + "      <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #2d3342; color: #8b949e; font-size: 14px;'>"
                + "        <p style='margin: 0; font-size: 16px; font-weight: bold; color: #5865f2; text-shadow: 0 0 8px rgba(88, 101, 242, 0.5);'>ndihehe</p>"
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</div>";
    }
}
