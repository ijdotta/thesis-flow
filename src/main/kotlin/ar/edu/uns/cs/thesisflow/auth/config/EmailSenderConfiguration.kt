package ar.edu.uns.cs.thesisflow.auth.config

import ar.edu.uns.cs.thesisflow.auth.service.EmailSender
import ar.edu.uns.cs.thesisflow.auth.service.SpringMailEmailSender
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

/**
 * Configuration for email sender.
 *
 * Provides SpringMailEmailSender as the EmailSender implementation.
 * Uses SMTP configuration from application properties.
 */
@Configuration
class EmailSenderConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Provide SpringMailEmailSender as the EmailSender.
     *
     * @param mailSender The configured JavaMailSender from Spring Boot
     * @return SpringMailEmailSender wrapped as EmailSender interface
     */
    @Bean
    fun emailSender(mailSender: JavaMailSender): EmailSender {
        logger.info("Email provider: Spring Mail SMTP")
        return SpringMailEmailSender(mailSender)
    }
}
