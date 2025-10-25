package ar.edu.uns.cs.thesisflow.projects.bulk

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun String.normalized(): String {
    return this.trim().replace(Regex("\\s+"), " ")
}

fun String.asProjectType(): ProjectType {
    return when (this.uppercase()) {
        "TL" -> ProjectType.THESIS
        "PF" -> ProjectType.FINAL_PROJECT
        else -> throw IllegalArgumentException("Unknown project: $this")
    }
}

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun String.asDate(): LocalDate? {
    if (isBlank()) return null
    return try {
        LocalDate.parse(this, DATE_FORMAT)
    } catch (e: Exception) {
        null
    }
}

fun String.asStringTags(): Set<String> {
    return this.split(",").map { it.normalized() }.filter { it.isNotBlank() }.toSet()
}