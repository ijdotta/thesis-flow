package ar.edu.uns.cs.thesisflow.people.persistance

import ar.edu.uns.cs.thesisflow.people.model.Person
import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository: JpaRepository<Person, Long>