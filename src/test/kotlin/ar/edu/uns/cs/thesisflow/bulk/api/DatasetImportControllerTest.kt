package ar.edu.uns.cs.thesisflow.bulk.api

import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(ar.edu.uns.cs.thesisflow.config.JpaAuditingConfig::class)
class DatasetImportControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val projectRepository: ProjectRepository,
) {

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `imports csv and reports successes and failures`() {
        val csvContent = """
            Tipo de proyecto,Fecha Consejo,Fecha Finalizacion,Completado,Titulo,Director,Co-Director,Colaborador,Alumno 1,Alumno 2,Alumno 3,Tema(s),Area o Dominio de Aplicacion
            PF,14/06/2011,14/09/2011,TRUE,Import Test,"ARDHENGI, Jorge",,,"FULLANA, Pablo",,,Desarrollo Mobile,Software
            XX,14/06/2011,,FALSE,Invalid Type,"ARDHENGI, Jorge",,,,,,Software
        """.trimIndent()
        val multipart = MockMultipartFile(
            "file",
            "test-dataset.csv",
            "text/csv",
            csvContent.toByteArray()
        )

        mockMvc.perform(
            multipart("/bulk/dataset/projects")
                .file(multipart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.summary.total").value(2))
            .andExpect(jsonPath("$.summary.success").value(1))
            .andExpect(jsonPath("$.summary.failed").value(1))
            .andExpect(jsonPath("$.results[0].status").value("SUCCESS"))
            .andExpect(jsonPath("$.results[0].project.publicId").isNotEmpty)
            .andExpect(jsonPath("$.results[1].status").value("FAILED"))
            .andExpect(jsonPath("$.results[1].message").value(containsString("Unknown project type")))

        assertThat(projectRepository.count()).isEqualTo(1)
    }
}
