package ar.edu.uns.cs.thesisflow.auth.service

import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.io.ByteArrayOutputStream
import java.util.*
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Gmail API email sender implementation.
 *
 * Uses Google Workspace Gmail API to send emails via authenticated OAuth2 credentials.
 * Requires:
 * - Google Cloud project with Gmail API enabled
 * - OAuth2 credentials (service account or user account) with Gmail API scope
 * - Credentials JSON file at the path specified by GMAIL_CREDENTIALS_PATH
 *
 * Implements [EmailSender] interface for pluggable injection.
 *
 * Note: This class is NOT annotated with @Service. Instead, it's instantiated
 * by the EmailSenderConfiguration based on gmail.enabled property.
 *
 * @see https://developers.google.com/workspace/gmail/api/quickstart/java
 */
class GmailEmailSender(
    private val enabled: Boolean,
    private val credentialsPath: String,
    private val userEmail: String
) : EmailSender {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var accessToken: String? = null
    private var tokenExpiry: Long = 0

    init {
        if (enabled && credentialsPath.isNotBlank()) {
            initializeGmailService()
        }
    }

    /**
     * Initialize Gmail service with OAuth2 credentials.
     * Loads credentials from the specified credentials file path.
     */
    private fun initializeGmailService() {
        try {
            val credentials = GoogleCredentials
                .fromStream(FileInputStream(credentialsPath))
                .createScoped(listOf("https://www.googleapis.com/auth/gmail.send"))

            // Force token refresh to validate credentials
            credentials.refresh()
            accessToken = credentials.accessToken.tokenValue
            tokenExpiry = credentials.accessToken.expirationTime?.time ?: 0

            logger.info("Gmail API service initialized successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize Gmail API service", e)
            throw RuntimeException("Failed to initialize Gmail API: ${e.message}")
        }
    }

    /**
     * Refresh access token if expired.
     */
    private fun refreshTokenIfNeeded(credentials: GoogleCredentials) {
        if (System.currentTimeMillis() > tokenExpiry) {
            credentials.refresh()
            accessToken = credentials.accessToken.tokenValue
            tokenExpiry = credentials.accessToken.expirationTime?.time ?: 0
        }
    }

    /**
     * Send email using Gmail API.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body content
     * @throws RuntimeException if email sending fails
     */
    override fun send(to: String, subject: String, htmlBody: String) {
        if (!enabled) {
            logger.warn("Gmail API is disabled. Configure gmail.enabled=true and provide credentials-path.")
            throw RuntimeException("Gmail API sender is not enabled")
        }

        if (accessToken.isNullOrBlank()) {
            throw RuntimeException("Gmail API service is not initialized")
        }

        try {
            val message = createMessage(to, subject, htmlBody)
            sendViaGmailApi(message)
            logger.info("Email sent via Gmail API to: $to")
        } catch (e: Exception) {
            logger.error("Failed to send email via Gmail API to: $to", e)
            throw RuntimeException("Failed to send email: ${e.message}")
        }
    }

    /**
     * Send message via Gmail API REST endpoint.
     *
     * @param encodedMessage Base64-encoded MIME message
     */
    private fun sendViaGmailApi(encodedMessage: String) {
        val url = URL("https://www.googleapis.com/gmail/v1/users/$userEmail/messages/send")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val requestBody = """{"raw":"$encodedMessage"}""".toByteArray()
            connection.outputStream.write(requestBody)
            connection.outputStream.flush()

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                val error = connection.errorStream?.bufferedReader()?.readText()
                throw RuntimeException("Gmail API error (HTTP $responseCode): $error")
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Create a MIME message and encode it to base64 for Gmail API.
     *
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlBody HTML email body
     * @return Base64-encoded MIME message
     */
    private fun createMessage(to: String, subject: String, htmlBody: String): String {
        val properties = Properties()
        val session = Session.getDefaultInstance(properties, null)
        val email = MimeMessage(session)

        email.setFrom(InternetAddress("noreply@thesisflow.example.com"))
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(htmlBody, "UTF-8", "html")

        val buffer = ByteArrayOutputStream()
        email.writeTo(buffer)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(buffer.toByteArray())
    }

}
