package ar.edu.uns.cs.thesisflow.auth.model

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "auth_user",
    indexes = [Index(name = "idx_auth_user_public_id", columnList = "public_id")]
)
class AuthUser(
    @Column(nullable = false, unique = true, length = 128)
    var username: String,
    @Column(nullable = false, length = 255)
    var password: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var role: UserRole,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    var professor: Professor? = null,
) : BaseEntity()
