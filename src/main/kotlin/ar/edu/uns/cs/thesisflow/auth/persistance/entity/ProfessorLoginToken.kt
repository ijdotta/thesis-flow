package ar.edu.uns.cs.thesisflow.auth.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "professor_login_token",
    indexes = [
        Index(name = "idx_professor_login_token", columnList = "token", unique = true),
        Index(name = "idx_professor_login_token_expires_at", columnList = "expires_at"),
        Index(name = "idx_professor_login_token_professor_id", columnList = "professor_id")
    ]
)
class ProfessorLoginToken(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    var professor: Professor,

    @Column(nullable = false, unique = true, length = 64)
    var token: String,

    @Column(nullable = false)
    var expiresAt: Instant,

    @Column(nullable = true)
    var usedAt: Instant? = null,
) : BaseEntity() {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isAlreadyUsed(): Boolean = usedAt != null

    fun markAsUsed(): ProfessorLoginToken {
        usedAt = Instant.now()
        return this
    }

    fun isValid(): Boolean = !isExpired() && !isAlreadyUsed()
}
