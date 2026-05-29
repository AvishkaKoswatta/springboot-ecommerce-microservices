package com.microservice.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Async("asyncTaskExecutor")
    public void sendOrderPlaced(String toEmail, String orderNumber, BigDecimal total) {
        String subject = "Order Received — " + orderNumber;
        String html = """
            <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2 style="color:#2c3e50">We received your order!</h2>
              <p>Hi, thanks for your order. We are awaiting payment confirmation.</p>
              <table style="width:100%;border-collapse:collapse;margin:20px 0">
                <tr><td style="padding:8px;color:#555">Order number</td>
                    <td style="padding:8px;font-weight:bold">%s</td></tr>
                <tr><td style="padding:8px;color:#555">Total</td>
                    <td style="padding:8px;font-weight:bold">£%s</td></tr>
              </table>
              <a href="%s/orders/%s" style="display:inline-block;background:#3498db;color:white;
                 padding:12px 24px;border-radius:5px;text-decoration:none;font-weight:bold">
                View Order
              </a>
            </body>
            """.formatted(orderNumber, total, frontendUrl, orderNumber);
        sendHtml(toEmail, subject, html);
    }

    @Async("asyncTaskExecutor")
    public void sendOrderCancelled(String toEmail, String orderNumber,
                                   String reason, boolean refundInitiated) {
        String subject = "Order Cancelled — " + orderNumber;
        String refundNote = refundInitiated
                ? "<p style='color:#e74c3c'>A refund has been initiated and will appear in 3–5 business days.</p>"
                : "";
        String html = """
            <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2 style="color:#2c3e50">Your order has been cancelled</h2>
              <p>Order <strong>%s</strong> has been cancelled.</p>
              <p style="color:#555">Reason: %s</p>
              %s
              <a href="%s/orders" style="display:inline-block;background:#3498db;color:white;
                 padding:12px 24px;border-radius:5px;text-decoration:none;font-weight:bold">
                View Orders
              </a>
            </body>
            """.formatted(orderNumber, reason, refundNote, frontendUrl);
        sendHtml(toEmail, subject, html);
    }

    @Async("asyncTaskExecutor")
    public void sendOrderShipped(String toEmail, String orderNumber, String trackingNumber) {
        String subject = "Your order is on its way — " + orderNumber;
        String html = """
            <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2 style="color:#2c3e50">Your order has been shipped!</h2>
              <p>Order <strong>%s</strong> is on its way to you.</p>
              <p>Tracking number: <strong>%s</strong></p>
              <a href="%s/orders/%s" style="display:inline-block;background:#27ae60;color:white;
                 padding:12px 24px;border-radius:5px;text-decoration:none;font-weight:bold">
                Track Order
              </a>
            </body>
            """.formatted(orderNumber, trackingNumber, frontendUrl, orderNumber);
        sendHtml(toEmail, subject, html);
    }

    @Async("asyncTaskExecutor")
    public void sendRefundProcessed(String toEmail, String orderNumber, BigDecimal amount) {
        String subject = "Refund Processed — " + orderNumber;
        String html = """
            <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <h2 style="color:#2c3e50">Your refund has been processed</h2>
              <p>A refund of <strong>£%s</strong> for order <strong>%s</strong>
                 has been processed.</p>
              <p style="color:#555">Please allow 3–5 business days for it to appear.</p>
            </body>
            """.formatted(amount, orderNumber);
        sendHtml(toEmail, subject, html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
