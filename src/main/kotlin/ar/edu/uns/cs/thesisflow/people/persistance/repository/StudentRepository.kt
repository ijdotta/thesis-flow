package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import org.springframework.data.jpa.repository.JpaRepository

interface StudentRepository: JpaRepository<Student, Long>