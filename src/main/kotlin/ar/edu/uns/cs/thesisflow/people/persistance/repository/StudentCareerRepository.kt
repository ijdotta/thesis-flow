package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import org.springframework.data.jpa.repository.JpaRepository

interface StudentCareerRepository: JpaRepository<StudentCareer, Long> {
    fun findAllByCareer(career: Career): List<StudentCareer>
}