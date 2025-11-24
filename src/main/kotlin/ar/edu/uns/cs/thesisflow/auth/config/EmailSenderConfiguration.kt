package ar.edu.uns.cs.thesisflow.auth.config

import ar.edu.uns.cs.thesisflow.auth.service.EmailSender
import ar.edu.uns.cs.thesisflow.auth.service.GmailEmailSender
import ar.edu.uns.cs.thesisflow.auth.service.SpringMailEmailSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

/**
 * Configuration for email sender selection.
 *
 * Determines which email provider to use based on application properties:
 * - If gmail.enabled=true → GmailEmailSender (Google Workspace Gmail API)
 * - Otherwise → SpringMailEmailSender (SMTP)
 *
 * Selection happens once at application startup and a single EmailSender bean is created.
 * This bean is then injected into EmailService.
 *
 * The selection is done via @ConditionalOnProperty, ensuring only ONE EmailSender bean
 * exists at any given time based on configuration.
 */
@Configuration
class EmailSenderConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Provide GmailEmailSender as the EmailSender when Gmail is enabled.
     *
     * @param credentialsPath Path to Gmail service account JSON credentials
     * @param userEmail Gmail account email to send from
     * @return GmailEmailSender wrapped as EmailSender interface
     */
    @Bean
    @ConditionalOnProperty("gmail.enabled", havingValue = "true")
    fun emailSender(
        @Value("\${gmail.credentials-path:}")
        credentialsPath: String,
        @Value("\${gmail.user-email:me}")
        userEmail: String,
    ): EmailSender {
        logger.info("Email provider: Gmail API (gmail.enabled=true)")
        return GmailEmailSender(
            credentialsPath = credentialsPath,
            userEmail = userEmail
        )
    }

    /**
     * Provide SpringMailEmailSender as the fallback EmailSender.
     *
     * Created if Gmail is not enabled. Exactly one EmailSender bean will exist.
     *
     * @param mailSender The configured JavaMailSender from Spring Boot
     * @return SpringMailEmailSender wrapped as EmailSender interface
     */
    @Bean
    @ConditionalOnProperty("gmail.enabled", havingValue = "false", matchIfMissing = true)
    fun emailSenderFallback(mailSender: JavaMailSender): EmailSender {
        logger.info("Email provider: Spring Mail SMTP (gmail.enabled=false or not set)")
        return SpringMailEmailSender(mailSender)
    }
}
