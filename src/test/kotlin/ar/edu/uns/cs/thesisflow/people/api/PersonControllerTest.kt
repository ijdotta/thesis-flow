package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.common.exceptions.ControllerExceptionHandler
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException
import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

class PersonControllerTest {

    private val personService: PersonService = mockk()
    private val mapper = jacksonObjectMapper()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(PersonController(personService))
        .setControllerAdvice(ControllerExceptionHandler())
        .setValidator(LocalValidatorFactoryBean())
        .build()

    @Test
    fun `create person success`() {
        val request = PersonDTO(name = "John", lastname = "Doe")
        val response = request.copy(id = 1, publicId = "pub-id-1")
        every { personService.create(request) } returns response

        mockMvc.perform(post("/people")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.publicId").value("pub-id-1"))
            .andExpect(jsonPath("$.name").value("John"))
    }

    @Test
    fun `create person validation failure`() {
        mockMvc.perform(post("/people")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(containsString("Validation failed")))
            .andExpect(content().string(containsString("name is required")))
            .andExpect(content().string(containsString("lastname is required")))
    }

    @Test
    fun `get person not found`() {
        every { personService.findByPublicId("missing") } throws NotFoundException("Person missing does not exist")

        mockMvc.perform(get("/people/missing"))
            .andExpect(status().isNotFound)
            .andExpect(content().string(containsString("Person missing does not exist")))
    }
}
