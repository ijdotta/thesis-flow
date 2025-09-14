package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationDomainRepository: JpaRepository<ApplicationDomain, Long>