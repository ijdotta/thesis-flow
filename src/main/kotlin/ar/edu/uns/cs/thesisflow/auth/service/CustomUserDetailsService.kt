package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.repository.AuthUserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val authUserRepository: AuthUserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = authUserRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User $username not found") }
        return AuthUserPrincipal.from(user)
    }
}
