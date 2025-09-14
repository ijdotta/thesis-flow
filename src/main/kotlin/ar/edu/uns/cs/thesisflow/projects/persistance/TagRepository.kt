package ar.edu.uns.cs.thesisflow.projects.persistance

import ar.edu.uns.cs.thesisflow.projects.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository: JpaRepository<Tag, Long>