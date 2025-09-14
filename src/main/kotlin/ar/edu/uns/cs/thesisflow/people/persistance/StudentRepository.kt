package ar.edu.uns.cs.thesisflow.people.persistance

import ar.edu.uns.cs.thesisflow.people.model.Student
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepository: JpaRepository<Student, Long>