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

    /**
     * Send professor login link email.
     *
     * @param professor Professor entity with email and name
     * @param loginLink The magic login link URL to include in email
     * @throws RuntimeException if email sending fails
     */
    fun sendProfessorLoginLink(professor: Professor, loginLink: String) {
        val emailBody = buildEmailBody(professor.person.name, loginLink)
        val subject = "Tu enlace mágico para acceder a Thesis Flow"

        try {
            emailSender.send(professor.email, subject, emailBody)
            logger.info("Login link email sent to: ${professor.email}")
        } catch (e: Exception) {
            logger.error("Failed to send login link email to: ${professor.email}", e)
            throw RuntimeException("Failed to send email: ${e.message}")
        }
    }

    private fun buildEmailBody(professorName: String, loginLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                body { font-family: Arial, sans-serif; }
                .container { max-width: 500px; margin: 0 auto; }
                .header { background: #007bff; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background: #f9f9f9; }
                .button { 
                  display: inline-block; 
                  background: #007bff; 
                  color: white; 
                  padding: 12px 30px; 
                  text-decoration: none;
                  border-radius: 5px;
                  margin: 20px 0;
                }
                .footer { font-size: 12px; color: #666; padding: 20px; text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🎓 Thesis Flow</h1>
                </div>
                
                <div class="content">
                  <h2>Hola $professorName,</h2>
                  
                  <p>Solicitaste un enlace para acceder a Thesis Flow. Haz clic en el botón de abajo para iniciar sesión:</p>
                  
                  <center>
                    <a href="$loginLink" class="button">
                      Iniciar sesión en Thesis Flow
                    </a>
                  </center>
                  
                  <p>O copia y pega este enlace en tu navegador:</p>
                  <p style="word-break: break-all; background: white; padding: 10px; border-radius: 3px;">
                    $loginLink
                  </p>
                  
                  <p><strong>⚠️ Importante:</strong></p>
                  <ul>
                    <li>Este enlace vence en <strong>15 minutos</strong></li>
                    <li>Este enlace solo se puede usar una vez</li>
                    <li>Si no solicitaste este enlace, puedes ignorar este email de forma segura</li>
                  </ul>
                </div>
                
                <div class="footer">
                  <p>© 2025 Thesis Flow. Todos los derechos reservados.</p>
                  <p>Este es un email automático, por favor no respondas.</p>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }
}
