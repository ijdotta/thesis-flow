package ar.edu.uns.cs.thesisflow.projects.api

data class ProjectResourceRequest(
    val url: String,
    val title: String,
    val description: String? = null,
)
