package ar.edu.uns.cs.thesisflow.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val expiration: Duration = Duration.ofHours(1),
)
