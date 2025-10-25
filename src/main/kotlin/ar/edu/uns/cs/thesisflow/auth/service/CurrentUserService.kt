package ar.edu.uns.cs.thesisflow.auth.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CurrentUserService {

    fun getCurrentUser(): AuthUserPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? AuthUserPrincipal

    fun requireCurrentUser(): AuthUserPrincipal =
        getCurrentUser() ?: throw IllegalStateException("No authenticated user in context")

    fun isAdmin(): Boolean = getCurrentUser()?.role == UserRole.ADMIN

    fun isProfessor(): Boolean = getCurrentUser()?.role == UserRole.PROFESSOR

    fun professorPublicId(): UUID? = getCurrentUser()?.professorPublicId
}
