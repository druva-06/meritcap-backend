package com.meritcap.service.impl;

import com.meritcap.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using JavaMail
 * Can be configured to send actual emails or return content in API response
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Value("${app.name:MeritCap}")
    private String appName;

    @Value("${app.email.sendActualEmails:false}")
    private boolean sendActualEmails;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        logger.info("EmailServiceImpl initialized. Send actual emails: {}", sendActualEmails);
    }

    @Override
    public EmailService.EmailDetails sendInvitationEmail(String email, String firstName, String invitationToken,
            int expiryHours) {
        try {
            String subject = String.format("You're invited to join %s", appName);
            String signupLink = String.format("%s/signup?token=%s", frontendUrl, invitationToken);

            String htmlContent = buildInvitationEmailTemplate(firstName, signupLink, expiryHours);

            if (sendActualEmails) {
                sendHtmlEmail(email, subject, htmlContent);
                logger.info("Invitation email sent successfully to: {}", email);
                return new EmailService.EmailDetails(email, subject, htmlContent, signupLink, true);
            } else {
                logger.info("📧 [DEV MODE] Invitation email captured (not sent) to: {}", email);
                logger.info("🔗 Signup Link: {}", signupLink);
                return new EmailService.EmailDetails(email, subject, htmlContent, signupLink, false);
            }
        } catch (Exception e) {
            logger.error("Failed to send invitation email to: {}", email, e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }

    @Override
    public EmailService.EmailDetails sendPasswordResetEmail(String email, String firstName, String resetToken) {
        try {
            String subject = String.format("Reset your %s password", appName);
            String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, resetToken);

            String htmlContent = buildPasswordResetEmailTemplate(firstName, resetLink);

            if (sendActualEmails) {
                sendHtmlEmail(email, subject, htmlContent);
                logger.info("Password reset email sent successfully to: {}", email);
                return new EmailService.EmailDetails(email, subject, htmlContent, resetLink, true);
            } else {
                logger.info("📧 [DEV MODE] Password reset email captured (not sent) to: {}", email);
                logger.info("🔗 Reset Link: {}", resetLink);
                return new EmailService.EmailDetails(email, subject, htmlContent, resetLink, false);
            }
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    public EmailService.EmailDetails sendWelcomeEmail(String email, String firstName) {
        String subject = String.format("Welcome to %s!", appName);
        String htmlContent = buildWelcomeEmailTemplate(firstName);

        try {
            if (sendActualEmails) {
                sendHtmlEmail(email, subject, htmlContent);
                logger.info("Welcome email sent successfully to: {}", email);
                return new EmailService.EmailDetails(email, subject, htmlContent, null, true);
            } else {
                logger.info("📧 [DEV MODE] Welcome email captured (not sent) to: {}", email);
                return new EmailService.EmailDetails(email, subject, htmlContent, null, false);
            }
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", email, e);
            // Don't throw exception for welcome email - it's not critical
            return new EmailService.EmailDetails(email, subject, htmlContent, null, false);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildInvitationEmailTemplate(String firstName, String signupLink, int expiryHours) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #4F46E5; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                                .button { display: inline-block; background-color: #4F46E5; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 20px 0; font-weight: bold; }
                                .button:hover { background-color: #4338CA; }
                                .footer { background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                                .warning { background-color: #FEF3C7; padding: 15px; border-left: 4px solid #F59E0B; margin: 20px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>%s</h1>
                                </div>
                                <div class="content">
                                    <h2>Hi %s,</h2>
                                    <p>You've been invited to join <strong>%s</strong>! We're excited to have you on board.</p>
                                    <p>Click the button below to complete your registration and set up your account:</p>
                                    <div style="text-align: center;">
                                        <a href="%s" class="button">Complete Registration</a>
                                    </div>
                                    <p>Or copy and paste this link into your browser:</p>
                                    <p style="background-color: white; padding: 12px; border: 1px solid #e5e7eb; border-radius: 4px; word-break: break-all; font-size: 14px;">
                                        %s
                                    </p>
                                    <div class="warning">
                                        <strong>⏰ Important:</strong> This invitation link will expire in <strong>%d hours</strong>. Please complete your registration before it expires.
                                    </div>
                                    <p>If you didn't expect this invitation, you can safely ignore this email.</p>
                                </div>
                                <div class="footer">
                                    <p>This is an automated email from %s. Please do not reply to this email.</p>
                                    <p>&copy; 2026 %s. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName, firstName, appName, signupLink, signupLink, expiryHours, appName, appName);
    }

    private String buildPasswordResetEmailTemplate(String firstName, String resetLink) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #DC2626; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                                .button { display: inline-block; background-color: #DC2626; color: white; padding: 14px 28px; text-decoration: none; border-radius: 6px; margin: 20px 0; font-weight: bold; }
                                .footer { background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                                .warning { background-color: #FEE2E2; padding: 15px; border-left: 4px solid #DC2626; margin: 20px 0; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Password Reset Request</h1>
                                </div>
                                <div class="content">
                                    <h2>Hi %s,</h2>
                                    <p>We received a request to reset your password for your %s account.</p>
                                    <p>Click the button below to reset your password:</p>
                                    <div style="text-align: center;">
                                        <a href="%s" class="button">Reset Password</a>
                                    </div>
                                    <div class="warning">
                                        <strong>⚠️ Security Notice:</strong> If you didn't request a password reset, please ignore this email and your password will remain unchanged.
                                    </div>
                                </div>
                                <div class="footer">
                                    <p>This is an automated email from %s. Please do not reply to this email.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                firstName, appName, resetLink, appName);
    }

    private String buildWelcomeEmailTemplate(String firstName) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #10B981; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                                .footer { background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>🎉 Welcome to %s!</h1>
                                </div>
                                <div class="content">
                                    <h2>Hi %s,</h2>
                                    <p>Welcome aboard! We're thrilled to have you join our community.</p>
                                    <p>Your account has been successfully created and you can now access all the features available to you.</p>
                                    <p>If you have any questions or need assistance, please don't hesitate to reach out to our support team.</p>
                                    <p>We're here to help you succeed!</p>
                                </div>
                                <div class="footer">
                                    <p>&copy; 2026 %s. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName, firstName, appName);
    }

    @Override
    public EmailService.EmailDetails sendOTPEmail(String email, String otp) {
        String subject = String.format("Your %s Login Code", appName);
        String htmlContent = buildOTPEmailTemplate(otp);

        try {
            if (sendActualEmails) {
                sendHtmlEmail(email, subject, htmlContent);
                logger.info("OTP email sent successfully to: {}", email);
                return new EmailService.EmailDetails(email, subject, htmlContent, null, true);
            } else {
                logger.info("📧 [DEV MODE] OTP email captured (not sent) to: {}", email);
                logger.info("🔑 OTP Code: {}", otp);
                return new EmailService.EmailDetails(email, subject, htmlContent, null, false);
            }
        } catch (Exception e) {
            logger.error("Failed to send OTP email to: {}", email, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOTPEmailTemplate(String otp) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background-color: #4F46E5; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                                .otp-box { background-color: #EEF2FF; border: 2px solid #4F46E5; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 8px; margin: 30px 0; border-radius: 8px; color: #4F46E5; }
                                .footer { background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; border-radius: 0 0 8px 8px; }
                                .warning { background-color: #FEF3C7; padding: 15px; border-left: 4px solid #F59E0B; margin: 20px 0; font-size: 14px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>🔐 Your Login Code</h1>
                                </div>
                                <div class="content">
                                    <p>Hello,</p>
                                    <p>You requested a login code for your %s account. Use the code below to complete your login:</p>
                                    <div class="otp-box">%s</div>
                                    <div class="warning">
                                        <strong>⏰ Valid for 10 minutes</strong><br>
                                        This code will expire in 10 minutes. If you didn't request this code, please ignore this email.
                                    </div>
                                    <p style="font-size: 14px; color: #6b7280;">For your security, never share this code with anyone.</p>
                                </div>
                                <div class="footer">
                                    <p>This is an automated email from %s. Please do not reply to this email.</p>
                                    <p>&copy; 2026 %s. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                appName, otp, appName, appName);
    }
}
