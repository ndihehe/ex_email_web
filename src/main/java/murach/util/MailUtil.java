package murach.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    public static String getResendApiKey() {
        return System.getenv("RESEND_API_KEY");
    }

    public static void sendMail(String to, String from,
                                String subject, String body, 
                                boolean bodyIsHTML) 
            throws MessagingException, UnsupportedEncodingException {

        // 1. Uu tien gui qua Resend REST API (Cong 443 HTTPS - Hoat dong 100% tren Render khong bi chan cong)
        String resendKey = getResendApiKey();
        if (resendKey != null && !resendKey.trim().isEmpty()) {
            try {
                boolean sent = sendViaResend(to, subject, body, resendKey);
                if (sent) {
                    System.out.println("Email sent successfully via Resend HTTPS API to: " + to);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Resend API failed, falling back to SMTP: " + e.getMessage());
            }
        }

        // 2. Du phong: Gui qua Gmail SMTP truyen thong (cho localhost hoac server khong chan port 465)
        sendViaSmtp(to, from, subject, body, bodyIsHTML);
    }

    private static boolean sendViaResend(String to, String subject, String body, String apiKey) throws Exception {
        String fromSender = System.getenv("RESEND_FROM");
        if (fromSender == null || fromSender.trim().isEmpty()) {
            fromSender = "onboarding@resend.dev";
        }

        String json = "{"
            + "\"from\":\"ndihehe <" + fromSender + ">\","
            + "\"to\":[\"" + escapeJson(to) + "\"],"
            + "\"subject\":\"" + escapeJson(subject) + "\","
            + "\"html\":\"" + escapeJson(body) + "\""
            + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        } else {
            System.err.println("Resend API error (" + response.statusCode() + "): " + response.body());
            throw new RuntimeException("Resend API error: " + response.body());
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static void sendViaSmtp(String to, String from,
                                   String subject, String body, 
                                   boolean bodyIsHTML) 
            throws MessagingException, UnsupportedEncodingException {

        final String username = getSenderEmail();
        final String password = getAppPassword();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new MessagingException("Chưa cấu hình biến môi trường MAIL_USERNAME hoặc MAIL_PASSWORD trên máy chủ!");
        }

        // Ep Java luon su dung IPv4 de ket noi Gmail
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
                + "  <div style='max-width: 580px; margin: 0 auto; background-color: #151922; border-radius: 16px; border: 1px solid #2d3342; overflow: hidden; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7);'>"
                + "    <div style='background-color: #0b0e14; padding: 25px 20px; text-align: center; border-bottom: 2px solid #00f3ff;'>"
                + "      <h1 style='margin: 0; font-size: 24px; font-weight: 800; color: #00f3ff; text-shadow: 0 0 10px #00f3ff, 0 0 20px #00f3ff, 0 0 30px rgba(0, 243, 255, 0.5); text-transform: uppercase; letter-spacing: 1.5px;'>Welcome to our email list</h1>"
                + "    </div>"
                + "    <div style='padding: 30px; color: #e6edf3; line-height: 1.8; font-size: 15px;'>"
                + "      <p style='margin-top: 0; font-size: 17px;'>Dear <b style='color: #00f3ff; font-weight: bold; text-shadow: 0 0 5px rgba(0, 243, 255, 0.4);'>" + user.getFirstName() + "</b>,</p>"
                + "      <p>Thanks for joining our email list. We'll make sure to send you announcements about new products and promotions.</p>"
                + "      <p>Have a great day and thanks again!</p>"
                + "      <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #2d3342; color: #8b949e; font-size: 14px;'>"
                + "        <p style='margin: 0; font-size: 16px; font-weight: bold; color: #00f3ff; text-shadow: 0 0 8px rgba(0, 243, 255, 0.5);'>ndihehe</p>"
                + "      </div>"
                + "    </div>"
                + "  </div>"
                + "</div>";
    }
}
