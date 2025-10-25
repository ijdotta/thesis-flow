package ar.edu.uns.cs.thesisflow.projects.bulk

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student

interface RawProjectsHandler {
    fun insertRawProject(rawProjectData: RawProjectData)

}