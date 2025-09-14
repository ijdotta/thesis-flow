package ar.edu.uns.cs.thesisflow.catalog.persistance.repository

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import org.springframework.data.jpa.repository.JpaRepository

interface CareerRepository: JpaRepository<Career, Long>