package ar.edu.uns.cs.thesisflow.auth.persistance.repository

import ar.edu.uns.cs.thesisflow.auth.persistance.entity.ProfessorLoginToken
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ProfessorLoginTokenRepository : JpaRepository<ProfessorLoginToken, Long> {
    fun findByToken(token: String): ProfessorLoginToken?

    fun findByProfessorAndTokenAndUsedAtIsNull(professor: Professor, token: String): ProfessorLoginToken?

    @Query("DELETE FROM ProfessorLoginToken t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(now: Instant)
}
