package ar.edu.uns.cs.thesisflow.projects.service

import java.text.Normalizer

object SearchNormalizer {
    fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("[\\p{M}]"), "").lowercase()
    }
}
