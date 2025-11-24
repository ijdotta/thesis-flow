package ar.edu.uns.cs.thesisflow.auth.service

/**
 * Email sender abstraction for pluggable email providers.
 *
 * Allows switching between different email implementations:
 * - GmailEmailSender (Google Workspace Gmail API)
 * - SpringMailEmailSender (SMTP via Spring Mail)
 * - Future: SendGrid, AWS SES, etc.
 *
 * At runtime, Spring injects the appropriate implementation based on configuration.
 */
interface EmailSender {
    /**
     * Send an HTML email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body content
     * @throws RuntimeException if email sending fails
     */
    fun send(to: String, subject: String, htmlBody: String)
}
