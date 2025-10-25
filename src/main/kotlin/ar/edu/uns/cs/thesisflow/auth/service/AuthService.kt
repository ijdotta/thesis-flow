package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
) {

    fun authenticate(username: String, password: String): Authentication {
        val authentication = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(authentication)
    }

    fun generateToken(authentication: Authentication): JwtToken {
        val principal = authentication.principal as? AuthUserPrincipal
            ?: throw IllegalStateException("Unexpected principal type: ${authentication.principal::class.simpleName}")
        return jwtService.generateToken(principal)
    }
}
