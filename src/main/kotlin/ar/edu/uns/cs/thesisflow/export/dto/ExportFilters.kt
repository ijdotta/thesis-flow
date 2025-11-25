package ar.edu.uns.cs.thesisflow.export.dto

data class ExportFilters(
    val careerIds: List<String>? = null,
    val applicationDomainIds: List<String>? = null,
    val tagIds: List<String>? = null,
    val professorIds: List<String>? = null,
    val projectTypes: List<String>? = null,
    val fromYear: Int? = null,
    val toYear: Int? = null,
)
