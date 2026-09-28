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

    public static String buildWelcomeEmail(murach.model.User user) {
        return "<div style='background-color: #0b0e14; padding: 35px 15px; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
                + "  <style>"
                + "    @keyframes discordRgb {"
                + "      0% { filter: hue-rotate(0deg); }"
                + "      100% { filter: hue-rotate(360deg); }"
                + "    }"
                + "    .discord-title {"
                + "      animation: discordRgb 3s linear infinite;"
                + "    }"
                + "  </style>"
                + "  <div style='max-width: 580px; margin: 0 auto; background-color: #151922; border-radius: 16px; border: 1px solid #2d3342; overflow: hidden; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7);'>"
                + "    <div style='background-color: #0b0e14; padding: 25px 20px; text-align: center; border-bottom: 2px solid #5865f2;'>"
                + "      <div style='display: inline-block; width: 100%; max-width: 540px;'>"
                + "        <svg width='100%' height='45' viewBox='0 0 540 45' xmlns='http://www.w3.org/2000/svg' style='display: block; margin: 0 auto;'>"
                + "          <defs>"
                + "            <linearGradient id='discordRainbow' x1='0%' y1='0%' x2='100%' y2='0%'>"
                + "              <stop offset='0%' stop-color='#ff007f'><animate attributeName='stop-color' values='#ff007f;#ff7b00;#ffee00;#00f3ff;#7928ca;#ff007f' dur='3s' repeatCount='indefinite'/></stop>"
                + "              <stop offset='25%' stop-color='#ff7b00'><animate attributeName='stop-color' values='#ff7b00;#ffee00;#00f3ff;#7928ca;#ff007f;#ff7b00' dur='3s' repeatCount='indefinite'/></stop>"
                + "              <stop offset='50%' stop-color='#00f3ff'><animate attributeName='stop-color' values='#00f3ff;#7928ca;#ff007f;#ff7b00;#ffee00;#00f3ff' dur='3s' repeatCount='indefinite'/></stop>"
                + "              <stop offset='75%' stop-color='#7928ca'><animate attributeName='stop-color' values='#7928ca;#ff007f;#ff7b00;#ffee00;#00f3ff;#7928ca' dur='3s' repeatCount='indefinite'/></stop>"
                + "              <stop offset='100%' stop-color='#ff007f'><animate attributeName='stop-color' values='#ff007f;#ff7b00;#ffee00;#00f3ff;#7928ca;#ff007f' dur='3s' repeatCount='indefinite'/></stop>"
                + "            </linearGradient>"
                + "            <filter id='neonGlow'>"
                + "              <feGaussianBlur stdDeviation='2.5' result='blur'/>"
                + "              <feMerge><feMergeNode in='blur'/><feMergeNode in='SourceGraphic'/></feMerge>"
                + "            </filter>"
                + "          </defs>"
                + "          <text x='50%' y='32' text-anchor='middle' fill='url(#discordRainbow)' filter='url(#neonGlow)' font-family='system-ui, -apple-system, sans-serif' font-size='23' font-weight='900' letter-spacing='2px'>WELCOME TO OUR EMAIL LIST</text>"
                + "        </svg>"
                + "      </div>"
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
