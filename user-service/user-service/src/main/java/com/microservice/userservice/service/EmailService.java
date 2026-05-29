package com.microservice.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Async("asyncTaskExecutor")
    public void sendEmailVerification(String toEmail, String username, String token) {
        String subject = "Verify Your Email Address";
        String verificationLink = frontendUrl + "/auth/verify-email?token=" + token;

        String htmlContent = buildEmailVerificationTemplate(username, verificationLink);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("asyncTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        String subject = "Reset Your Password";
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;

        String htmlContent = buildPasswordResetTemplate(username, resetLink);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("asyncTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to Our Platform!";
        String htmlContent = buildWelcomeTemplate(username);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async("asyncTaskExecutor")
    public void sendPasswordChangedNotification(String toEmail, String username) {
        String subject = "Your Password Has Been Changed";
        String htmlContent = buildPasswordChangedTemplate(username);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmailVerificationTemplate(String username, String verificationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 30px; text-align: center;">
                        <h2 style="color: #2c3e50;">Email Verification</h2>
                        <p style="color: #555;">Hi <strong>%s</strong>,</p>
                        <p style="color: #555;">Thank you for registering. Please verify your email address by clicking the button below:</p>
                        <a href="%s"
                           style="display: inline-block; background-color: #3498db; color: white; padding: 12px 30px;
                                  border-radius: 5px; text-decoration: none; font-weight: bold; margin: 20px 0;">
                            Verify Email
                        </a>
                        <p style="color: #888; font-size: 14px;">This link will expire in 24 hours.</p>
                        <p style="color: #888; font-size: 12px;">If you did not create an account, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(username, verificationLink);
    }

    private String buildPasswordResetTemplate(String username, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 30px; text-align: center;">
                        <h2 style="color: #2c3e50;">Password Reset Request</h2>
                        <p style="color: #555;">Hi <strong>%s</strong>,</p>
                        <p style="color: #555;">We received a request to reset your password. Click the button below to proceed:</p>
                        <a href="%s"
                           style="display: inline-block; background-color: #e74c3c; color: white; padding: 12px 30px;
                                  border-radius: 5px; text-decoration: none; font-weight: bold; margin: 20px 0;">
                            Reset Password
                        </a>
                        <p style="color: #888; font-size: 14px;">This link will expire in 30 minutes.</p>
                        <p style="color: #888; font-size: 12px;">If you did not request a password reset, please ignore this email and your password will remain unchanged.</p>
                    </div>
                </body>
                </html>
                """.formatted(username, resetLink);
    }

    private String buildWelcomeTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 30px; text-align: center;">
                        <h2 style="color: #27ae60;">Welcome Aboard! 🎉</h2>
                        <p style="color: #555;">Hi <strong>%s</strong>,</p>
                        <p style="color: #555;">Your email has been verified and your account is now active. Welcome to our platform!</p>
                        <a href="%s/dashboard"
                           style="display: inline-block; background-color: #27ae60; color: white; padding: 12px 30px;
                                  border-radius: 5px; text-decoration: none; font-weight: bold; margin: 20px 0;">
                            Go to Dashboard
                        </a>
                    </div>
                </body>
                </html>
                """.formatted(username, frontendUrl);
    }

    private String buildPasswordChangedTemplate(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #f8f9fa; border-radius: 8px; padding: 30px; text-align: center;">
                        <h2 style="color: #2c3e50;">Password Changed</h2>
                        <p style="color: #555;">Hi <strong>%s</strong>,</p>
                        <p style="color: #555;">Your password has been successfully changed.</p>
                        <p style="color: #e74c3c; font-size: 14px;">If you did not make this change, please contact support immediately.</p>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }
}
