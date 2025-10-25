package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.config.JwtProperties
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.io.DecodingException
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.Instant

data class JwtToken(
    val token: String,
    val expiresAt: Instant,
)

@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val signingKey: Key by lazy {
        // Accept either raw string or Base64 encoded secret
        val secret = jwtProperties.secret
        val keyBytes = try {
            Decoders.BASE64.decode(secret)
        } catch (_: DecodingException) {
            secret.toByteArray(StandardCharsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            secret.toByteArray(StandardCharsets.UTF_8)
        }
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(principal: AuthUserPrincipal): JwtToken {
        val issuedAt = Instant.now()
        val expiration = issuedAt.plus(jwtProperties.expiration)
        val token = Jwts.builder()
            .setSubject(principal.username)
            .setIssuedAt(java.util.Date.from(issuedAt))
            .setExpiration(java.util.Date.from(expiration))
            .claim("role", principal.role.name)
            .claim("userId", principal.publicId.toString())
            .claim("professorId", principal.professorPublicId?.toString())
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
        return JwtToken(token, expiration)
    }

    fun extractUsername(token: String): String? = parseClaims(token)?.subject

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username != null && username == userDetails.username && !isTokenExpired(token)
    }

    private fun parseClaims(token: String): Claims? = try {
        Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    } catch (_: ExpiredJwtException) {
        null
    } catch (_: Exception) {
        null
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = parseClaims(token)?.expiration?.toInstant() ?: return true
        return expiration.isBefore(Instant.now())
    }
}
