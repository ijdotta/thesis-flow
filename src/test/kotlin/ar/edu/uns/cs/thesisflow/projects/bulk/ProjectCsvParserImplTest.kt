package ar.edu.uns.cs.thesisflow.projects.bulk

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream

class ProjectCsvParserImplTest {

    private val parser = ProjectCsvParserImpl()

    @Test
    fun `should parse valid CSV with all fields`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Machine Learning Research,Dr. Smith,Dr. Jones,Dr. Brown,Alice,Bob,Charlie,AI;ML,Computer Science
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.type).isEqualTo("THESIS")
        assertThat(project.submissionDate).isEqualTo("2024-01-15")
        assertThat(project.completionDate).isEqualTo("2024-06-30")
        assertThat(project.title).isEqualTo("Machine Learning Research")
        assertThat(project.director).isEqualTo("Dr. Smith")
        assertThat(project.codirector).isEqualTo("Dr. Jones")
        assertThat(project.collaborator).isEqualTo("Dr. Brown")
        assertThat(project.studentA).isEqualTo("Alice")
        assertThat(project.studentB).isEqualTo("Bob")
        assertThat(project.studentC).isEqualTo("Charlie")
        assertThat(project.topics).isEqualTo("AI;ML")
        assertThat(project.domain).isEqualTo("Computer Science")
    }

    @Test
    fun `should parse CSV with multiple projects`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Machine Learning Research,Dr. Smith,Dr. Jones,Dr. Brown,Alice,Bob,Charlie,AI;ML,Computer Science
            PROJECT,2024-02-20,2024-07-15,Database Optimization,Dr. White,,,Dave,Eve,Frank,Databases,Software Engineering
            INTERNSHIP,2024-03-10,2024-08-20,Web Development,Dr. Black,Dr. Green,Dr. Yellow,George,,,Web;Frontend,Web Development
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(3)
        assertThat(result[0].title).isEqualTo("Machine Learning Research")
        assertThat(result[1].title).isEqualTo("Database Optimization")
        assertThat(result[2].title).isEqualTo("Web Development")
    }

    @Test
    fun `should parse CSV with optional fields missing`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Simple Project,Dr. Smith,,,Alice,Bob,,,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.title).isEqualTo("Simple Project")
        assertThat(project.codirector).isEmpty()
        assertThat(project.collaborator).isEmpty()
        assertThat(project.studentC).isEmpty()
    }

    @Test
    fun `should trim whitespace from fields`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS  ,  2024-01-15  ,  2024-06-30  ,  Trimmed Title  ,  Dr. Smith  ,  Dr. Jones  ,  Dr. Brown  ,  Alice  ,  Bob  ,  Charlie  ,  AI  ,  CS  
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.type).isEqualTo("THESIS")
        assertThat(project.submissionDate).isEqualTo("2024-01-15")
        assertThat(project.title).isEqualTo("Trimmed Title")
        assertThat(project.director).isEqualTo("Dr. Smith")
    }

    @Test
    fun `should handle empty CSV with only headers`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should handle special characters in fields`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,"Title with, comma",Dr. O'Brien,"Dr. García",,"Student A",Student B,Student C,"Topic1;Topic2;Topic3","Data, Science"
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.title).isEqualTo("Title with, comma")
        assertThat(project.director).isEqualTo("Dr. O'Brien")
        assertThat(project.codirector).isEqualTo("Dr. García")
        assertThat(project.topics).isEqualTo("Topic1;Topic2;Topic3")
        assertThat(project.domain).isEqualTo("Data, Science")
    }

    @Test
    fun `should handle quoted fields with newlines`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,"Multi-line
            Title",Dr. Smith,,,Alice,Bob,Charlie,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).contains("Multi-line")
    }

    @Test
    fun `should handle CSV with different column order`() {
        // Given
        val csvContent = """
            title,director,student_a,student_b,student_c,project_type,submission_date,completion_date,codirector,collaborator,topics,domain
            Test Project,Dr. Test,Student1,Student2,Student3,THESIS,2024-01-01,2024-12-31,,,Testing,QA
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.title).isEqualTo("Test Project")
        assertThat(project.director).isEqualTo("Dr. Test")
        assertThat(project.type).isEqualTo("THESIS")
    }

    @Test
    fun `should parse CSV with UTF-8 encoded content`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Análisis de Datos,Dr. García,Dr. López,,José,María,Sofía,Análisis,Ciencia
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.title).isEqualTo("Análisis de Datos")
        assertThat(project.director).isEqualTo("Dr. García")
        assertThat(project.codirector).isEqualTo("Dr. López")
        assertThat(project.studentA).isEqualTo("José")
        assertThat(project.studentB).isEqualTo("María")
        assertThat(project.studentC).isEqualTo("Sofía")
    }

    @Test
    fun `should handle empty file`() {
        // Given
        val csvContent = ""
        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should handle CSV with only one student`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Solo Project,Dr. Smith,,,Alice,,,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.studentA).isEqualTo("Alice")
        assertThat(project.studentB).isEmpty()
        assertThat(project.studentC).isEmpty()
    }

    @Test
    fun `should handle CSV with all optional fields empty`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Minimal Project,Dr. Smith,,,Alice,Bob,Charlie,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.codirector).isEmpty()
        assertThat(project.collaborator).isEmpty()
    }

    @Test
    fun `should handle large CSV file with many rows`() {
        // Given
        val header = "project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain"
        val rows = (1..100).map { i ->
            "THESIS,2024-01-15,2024-06-30,Project $i,Director $i,,,Student A$i,Student B$i,Student C$i,Topic $i,Domain $i"
        }
        val csvContent = listOf(header).plus(rows).joinToString("\n")
        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(100)
        assertThat(result[0].title).isEqualTo("Project 1")
        assertThat(result[99].title).isEqualTo("Project 100")
    }

    @Test
    fun `should handle CSV with extra columns`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain,extra_column
            THESIS,2024-01-15,2024-06-30,Test Project,Dr. Smith,,,Alice,Bob,Charlie,AI,CS,Extra Data
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Test Project")
    }

    @Test
    fun `should handle CSV with semicolons in fields`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Research Project,Dr. Smith,,,Alice,Bob,Charlie,"AI;Machine Learning;Deep Learning",Computer Science
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].topics).isEqualTo("AI;Machine Learning;Deep Learning")
    }

    @Test
    fun `should skip CSV with empty lines between records`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Project 1,Dr. Smith,,,Alice,Bob,Charlie,AI,CS
            
            PROJECT,2024-02-20,2024-07-15,Project 2,Dr. White,,,Dave,Eve,Frank,DB,SE
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(2) // Including the empty line as a record
        assertThat(result[0].title).isEqualTo("Project 1")
        assertThat(result[1].title).isEqualTo("Project 2")
    }

    @Test
    fun `should preserve field values exactly as provided`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Project with 123 Numbers!,Dr. Smith Jr.,,,Alice-Marie,Bob O'Connor,Charlie (Grad),AI/ML,CS & IT
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        assertThat(project.title).isEqualTo("Project with 123 Numbers!")
        assertThat(project.director).isEqualTo("Dr. Smith Jr.")
        assertThat(project.studentA).isEqualTo("Alice-Marie")
        assertThat(project.studentB).isEqualTo("Bob O'Connor")
        assertThat(project.studentC).isEqualTo("Charlie (Grad)")
        assertThat(project.topics).isEqualTo("AI/ML")
        assertThat(project.domain).isEqualTo("CS & IT")
    }

    @Test
    fun `should handle CSV with only required fields`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,Minimal Project,Dr. Smith,Alice,Bob,Charlie,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When & Then - This should succeed with AllowMissingColumnNames set to true
        // The parser will handle missing columns gracefully
        val result = parser.readProjectsFromCsv(file)
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should handle CSV with tabs and spaces`() {
        // Given
        val csvContent = """
            project_type,submission_date,completion_date,title,director,codirector,collaborator,student_a,student_b,student_c,topics,domain
            THESIS,2024-01-15,2024-06-30,	Tabbed Title	,  Dr. Smith  ,,,  Alice  ,  Bob  ,  Charlie  ,AI,CS
        """.trimIndent()

        val file = createMultipartFile(csvContent)

        // When
        val result = parser.readProjectsFromCsv(file)

        // Then
        assertThat(result).hasSize(1)
        val project = result[0]
        // Trim should handle surrounding spaces
        assertThat(project.title.trim()).isEqualTo("Tabbed Title")
        assertThat(project.director).isEqualTo("Dr. Smith")
        assertThat(project.studentA).isEqualTo("Alice")
    }

    private fun createMultipartFile(content: String): MultipartFile {
        val mockFile = mockk<MultipartFile>()
        every { mockFile.inputStream } returns ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        return mockFile
    }
}
