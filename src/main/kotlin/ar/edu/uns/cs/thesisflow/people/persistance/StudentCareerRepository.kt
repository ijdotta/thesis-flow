package ar.edu.uns.cs.thesisflow.people.persistance

import ar.edu.uns.cs.thesisflow.people.model.StudentCareer
import org.springframework.data.jpa.repository.JpaRepository

interface StudentCareerRepository: JpaRepository<StudentCareer, Long>