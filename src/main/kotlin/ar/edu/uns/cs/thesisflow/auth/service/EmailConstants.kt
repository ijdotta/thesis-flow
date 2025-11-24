package ar.edu.uns.cs.thesisflow.auth.service

/**
 * Constants for email service configuration and templates.
 */
object EmailConstants {
    // Sender configuration
    const val SENDER_EMAIL = "noreply.thesisflow.dcic@gmail.com"
    const val APPLICATION_NAME = "Thesis Flow"
    
    // Email subjects
    const val SUBJECT_PROFESSOR_LOGIN_LINK = "Tu enlace mágico para acceder a Thesis Flow"
    
    // Token configuration
    const val TOKEN_EXPIRY_MINUTES = 15

    // Email template messages
    const val MESSAGE_GREETING = "Hola %s,"
    const val MESSAGE_REQUEST = "Solicitaste un enlace para acceder a Thesis Flow. Haz clic en el botón de abajo para iniciar sesión:"
    const val MESSAGE_BUTTON_TEXT = "Iniciar sesión en Thesis Flow"
    const val MESSAGE_OR_COPY = "O copia y pega este enlace en tu navegador:"
    const val MESSAGE_IMPORTANT = "Importante:"
    const val MESSAGE_EXPIRES = "Este enlace vence en %d minutos"
    const val MESSAGE_ONCE = "Este enlace solo se puede usar una vez"
    const val MESSAGE_IGNORE = "Si no solicitaste este enlace, puedes ignorar este email de forma segura"
    const val MESSAGE_COPYRIGHT = "© 2025 Thesis Flow. Todos los derechos reservados."
    const val MESSAGE_AUTO = "Este es un email automático, por favor no respondas."
    
    // HTML styling
    const val STYLE_BODY = "font-family: Arial, sans-serif;"
    const val STYLE_CONTAINER = "max-width: 500px; margin: 0 auto;"
    const val STYLE_HEADER = "background: #007bff; color: white; padding: 20px; text-align: center;"
    const val STYLE_CONTENT = "padding: 20px; background: #f9f9f9;"
    const val STYLE_BUTTON = "display: inline-block; background: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0;"
    const val STYLE_FOOTER = "font-size: 12px; color: #666; padding: 20px; text-align: center;"
    const val STYLE_LINK_BOX = "word-break: break-all; background: white; padding: 10px; border-radius: 3px;"
}
