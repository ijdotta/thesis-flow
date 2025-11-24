package ar.edu.uns.cs.thesisflow.auth.service

/**
 * Builder for HTML email templates.
 * Centralizes email HTML structure and styling.
 */
class EmailTemplateBuilder {

    fun buildProfessorLoginLinkTemplate(professorName: String, loginLink: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                body { ${EmailConstants.STYLE_BODY} }
                .container { ${EmailConstants.STYLE_CONTAINER} }
                .header { ${EmailConstants.STYLE_HEADER} }
                .content { ${EmailConstants.STYLE_CONTENT} }
                .button { ${EmailConstants.STYLE_BUTTON} }
                .footer { ${EmailConstants.STYLE_FOOTER} }
                .link-box { ${EmailConstants.STYLE_LINK_BOX} }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🎓 ${EmailConstants.APPLICATION_NAME}</h1>
                </div>
                
                <div class="content">
                  <h2>${String.format(EmailConstants.MESSAGE_GREETING, professorName)}</h2>
                  
                  <p>${EmailConstants.MESSAGE_REQUEST}</p>
                  
                  <center>
                    <a href="$loginLink" class="button">
                      ${EmailConstants.MESSAGE_BUTTON_TEXT}
                    </a>
                  </center>
                  
                  <p>${EmailConstants.MESSAGE_OR_COPY}</p>
                  <p class="link-box">
                    $loginLink
                  </p>
                  
                  <p><strong>⚠️ ${EmailConstants.MESSAGE_IMPORTANT}</strong></p>
                  <ul>
                    <li>${String.format(EmailConstants.MESSAGE_EXPIRES, EmailConstants.TOKEN_EXPIRY_MINUTES)}</li>
                    <li>${EmailConstants.MESSAGE_ONCE}</li>
                    <li>${EmailConstants.MESSAGE_IGNORE}</li>
                  </ul>
                </div>
                
                <div class="footer">
                  <p>${EmailConstants.MESSAGE_COPYRIGHT}</p>
                  <p>${EmailConstants.MESSAGE_AUTO}</p>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }
}
