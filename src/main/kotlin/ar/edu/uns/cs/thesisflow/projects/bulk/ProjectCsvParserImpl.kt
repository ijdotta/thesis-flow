package ar.edu.uns.cs.thesisflow.projects.bulk

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets

private const val PROJECT_TYPE = "project_type"
private const val SUBMISSION_DATE = "submission_date"
private const val COMPLETION_DATE = "completion_date"
private const val TITLE = "title"
private const val DIRECTOR = "director"
private const val CODIRECTOR = "codirector"
private const val COLLABORATOR = "collaborator"
private const val STUDENT_A = "student_a"
private const val STUDENT_B = "student_b"
private const val STUDENT_C = "student_c"
private const val TOPICS = "topics"
private const val DOMAIN = "domain"

class ProjectCsvParserImpl : ProjectCsvParser {
    private val format = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreSurroundingSpaces(true)
        .setTrim(true)
        .setAllowMissingColumnNames(true)
        .get()

    override fun readProjectsFromCsv(file: MultipartFile): List<RawProjectData> {
        return file.inputStream.use { input ->
            CSVParser.parse(input, StandardCharsets.UTF_8, format).use { parser ->
                parser.records.map { record ->
                    RawProjectData(
                        type = record.get(PROJECT_TYPE),
                        submissionDate = record.get(SUBMISSION_DATE),
                        completionDate = record.get(COMPLETION_DATE),
                        title = record.get(TITLE),
                        director = record.get(DIRECTOR),
                        codirector = record.get(CODIRECTOR),
                        collaborator = record.get(COLLABORATOR),
                        studentA = record.get(STUDENT_A),
                        studentB = record.get(STUDENT_B),
                        studentC = record.get(STUDENT_C),
                        topics = record.get(TOPICS),
                        domain = record.get(DOMAIN),
                    )
                }.toList()
            }
        }
    }
}