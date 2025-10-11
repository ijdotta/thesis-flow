package ar.edu.uns.cs.thesisflow.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    var allowedOrigins: List<String> = listOf("http://localhost:5173"),
    var allowedMethods: List<String> = listOf("GET","POST","PUT","PATCH","DELETE","OPTIONS"),
    var allowCredentials: Boolean = true,
)

