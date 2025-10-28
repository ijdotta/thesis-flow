package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import jakarta.mail.internet.MimeMessage

@Service
class EmailService(
    private val mailSender: JavaMailSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendProfessorLoginLink(professor: Professor, loginLink: String) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")

            helper.setTo(professor.email)
            helper.setSubject("Your Magic Login Link for Thesis Flow")
            helper.setText(buildEmailBody(professor.person.name, loginLink), true)
            helper.setFrom("noreply@thesisflow.example.com")

            mailSender.send(message)
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
                  <h2>Hello $professorName,</h2>
                  
                  <p>You requested a login link to access Thesis Flow. Click the button below to login:</p>
                  
                  <center>
                    <a href="$loginLink" class="button">
                      Login to Thesis Flow
                    </a>
                  </center>
                  
                  <p>Or copy and paste this link in your browser:</p>
                  <p style="word-break: break-all; background: white; padding: 10px; border-radius: 3px;">
                    $loginLink
                  </p>
                  
                  <p><strong>⚠️ Important:</strong></p>
                  <ul>
                    <li>This link expires in <strong>15 minutes</strong></li>
                    <li>This link can only be used once</li>
                    <li>If you didn't request this link, you can safely ignore this email</li>
                  </ul>
                </div>
                
                <div class="footer">
                  <p>© 2025 Thesis Flow. All rights reserved.</p>
                  <p>This is an automated email, please do not reply.</p>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }
}
