package ar.edu.uns.cs.thesisflow.projects.dto

data class ProjectResource(
    val url: String,
    val title: String,
    val description: String? = null,
)
