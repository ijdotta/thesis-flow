package ar.edu.uns.cs.thesisflow.auth.config

import ar.edu.uns.cs.thesisflow.auth.service.CustomUserDetailsService
import ar.edu.uns.cs.thesisflow.auth.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substringAfter("Bearer ").trim()
        val username = jwtService.extractUsername(token)
        val context = SecurityContextHolder.getContext()
        if (!username.isNullOrBlank() && context.authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(username)
            if (jwtService.isTokenValid(token, userDetails)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                context.authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
