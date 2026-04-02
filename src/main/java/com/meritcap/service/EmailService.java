package com.meritcap.service;

/**
 * Service interface for sending emails
 */
public interface EmailService {

    /**
     * Email details DTO for API responses
     */
    class EmailDetails {
        private String recipientEmail;
        private String subject;
        private String htmlContent;
        private String signupLink; // For invitation emails
        private boolean sent;

        public EmailDetails(String recipientEmail, String subject, String htmlContent, String signupLink,
                boolean sent) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.htmlContent = htmlContent;
            this.signupLink = signupLink;
            this.sent = sent;
        }

        public String getRecipientEmail() {
            return recipientEmail;
        }

        public String getSubject() {
            return subject;
        }

        public String getHtmlContent() {
            return htmlContent;
        }

        public String getSignupLink() {
            return signupLink;
        }

        public boolean isSent() {
            return sent;
        }
    }

    /**
     * Send invitation email with signup link
     * 
     * @param email           Recipient email address
     * @param firstName       Recipient first name
     * @param invitationToken Token to include in signup link
     * @param expiryHours     Hours until invitation expires
     * @return EmailDetails containing email content and signup link
     */
    EmailDetails sendInvitationEmail(String email, String firstName, String invitationToken, int expiryHours);

    /**
     * Send password reset email
     * 
     * @param email      Recipient email address
     * @param firstName  Recipient first name
     * @param resetToken Password reset token
     * @return EmailDetails containing email content
     */
    EmailDetails sendPasswordResetEmail(String email, String firstName, String resetToken);

    /**
     * Send welcome email after successful signup
     * 
     * @param email     Recipient email address
     * @param firstName Recipient first name
     * @return EmailDetails containing email content
     */
    EmailDetails sendWelcomeEmail(String email, String firstName);

    /**
     * Send OTP email for quick login
     * 
     * @param email Recipient email address
     * @param otp   6-digit OTP code
     * @return EmailDetails containing email content
     */
    EmailDetails sendOTPEmail(String email, String otp);
}
