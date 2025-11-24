package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Email service for sending transactional emails.
 *
 * Delegates to a single [EmailSender] implementation that is selected at runtime
 * based on configuration. This allows transparent switching between:
 * - Gmail API (via GmailEmailSender)
 * - SMTP (via SpringMailEmailSender)
 *
 * The actual implementation selection is handled by [CompositeEmailSender].
 *
 * @param emailSender The email sender implementation (injected by Spring)
 */
@Service
class EmailService(
    private val emailSender: EmailSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val templateBuilder = EmailTemplateBuilder()

    /**
     * Send professor login link email.
     *
     * @param professor Professor entity with email and name
     * @param loginLink The magic login link URL to include in email
     * @throws RuntimeException if email sending fails
     */
    fun sendProfessorLoginLink(professor: Professor, loginLink: String) {
        val emailBody = templateBuilder.buildProfessorLoginLinkTemplate(professor.person.name, loginLink)
        val subject = EmailConstants.SUBJECT_PROFESSOR_LOGIN_LINK

        try {
            emailSender.send(professor.email, subject, emailBody)
            logger.info("Login link email sent to: ${professor.email}")
        } catch (e: Exception) {
            logger.error("Failed to send login link email to: ${professor.email}", e)
            throw RuntimeException("Failed to send email: ${e.message}")
        }
    }
}
