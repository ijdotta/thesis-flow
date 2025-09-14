package ar.edu.uns.cs.thesisflow.catalog.persistance

import ar.edu.uns.cs.thesisflow.catalog.model.Career
import org.springframework.data.jpa.repository.JpaRepository

interface CareerRepository: JpaRepository<Career, Long>