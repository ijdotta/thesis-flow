package ar.edu.uns.cs.thesisflow.projects.persistance

import ar.edu.uns.cs.thesisflow.projects.model.ApplicationDomain
import org.springframework.data.jpa.repository.JpaRepository

interface ApplicationDomainRepository: JpaRepository<ApplicationDomain, Long>