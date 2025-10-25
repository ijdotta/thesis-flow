package ar.edu.uns.cs.thesisflow.auth.repository

import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AuthUserRepository : JpaRepository<AuthUser, Long> {
    fun findByUsername(username: String): Optional<AuthUser>
}
