package ar.edu.uns.cs.thesisflow.auth.service

import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import jakarta.mail.internet.MimeMessage

/**
 * Spring Mail SMTP email sender implementation.
 *
 * Uses configured SMTP server to send emails.
 * Configuration via spring.mail.* properties.
 *
 * Implements [EmailSender] interface for pluggable injection.
 *
 * Note: This class is NOT annotated with @Service. Instead, it's instantiated
 * by the EmailSenderConfiguration based on gmail.enabled property.
 *
 * @see https://spring.io/guides/gs/sending-email/
 */
class SpringMailEmailSender(
    private val mailSender: JavaMailSender,
) : EmailSender {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Send email via Spring Mail SMTP backend.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     * @throws RuntimeException if email sending fails
     */
    override fun send(to: String, subject: String, htmlBody: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)
            helper.setFrom(EmailConstants.SENDER_EMAIL)

            mailSender.send(message)
            logger.info("Email sent via Spring Mail to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email via Spring Mail to: $to", e)
            throw RuntimeException("Failed to send email: ${e.message}")
        }
    }
}
