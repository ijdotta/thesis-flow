package ar.edu.uns.cs.thesisflow.auth.service

/**
 * Abstract email sender interface.
 * Allows switching between different email providers (Spring Mail, Gmail API, etc.)
 */
interface IEmailSender {
    /**
     * Send an HTML email.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body content
     * @throws RuntimeException if email sending fails
     */
    fun send(to: String, subject: String, htmlBody: String)

    /**
     * Check if this email sender is enabled and ready to use.
     *
     * @return true if sender is available
     */
    fun isEnabled(): Boolean
}
