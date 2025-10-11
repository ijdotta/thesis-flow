package ar.edu.uns.cs.thesisflow.projects.bulk

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import org.springframework.web.multipart.MultipartFile

class ProjectCsvParserImpl : ProjectCsvParser {
    override fun readProjectsFromCsv(file: MultipartFile): List<BulkProjectData> {
    }
}