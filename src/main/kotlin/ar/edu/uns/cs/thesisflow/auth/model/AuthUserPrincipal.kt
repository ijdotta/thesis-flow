package ar.edu.uns.cs.thesisflow.auth.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class AuthUserPrincipal private constructor(
    private val id: Long?,
    val publicId: UUID,
    private val usernameValue: String,
    private val passwordValue: String,
    val role: UserRole,
    val professorPublicId: UUID?,
    val professorPersonPublicId: UUID?,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = passwordValue
    override fun getUsername(): String = usernameValue

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    fun getId(): Long? = id

    companion object {
        fun from(user: AuthUser): AuthUserPrincipal = AuthUserPrincipal(
            id = user.id,
            publicId = user.publicId,
            usernameValue = user.username,
            passwordValue = user.password,
            role = user.role,
            professorPublicId = user.professor?.publicId,
            professorPersonPublicId = user.professor?.person?.publicId
        )
    }
}
