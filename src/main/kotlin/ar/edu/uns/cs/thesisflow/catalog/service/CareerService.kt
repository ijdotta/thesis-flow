package ar.edu.uns.cs.thesisflow.catalog.service

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.dto.toDTO
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

@Service
class CareerService(
    private val careerRepository: CareerRepository,
) {
    fun findAll(pageable: Pageable): Page<CareerDTO> = careerRepository.findAll(pageable).map { it.toDTO() }

    fun findAllIn(publicIds: List<String>) = publicIds.map { UUID.fromString(it) }
        .let { careerRepository.findAllByPublicIdIn(it) }
        .map { it.toDTO() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(publicId).toDTO()

    fun findEntityByPublicId(publicId: String?) = publicId?.let {
        careerRepository.findByPublicId(UUID.fromString(it))
    } ?: throw IllegalArgumentException("Career with $publicId not found")

    fun create(careerDTO: CareerDTO): CareerDTO {
        validate(careerDTO)
        val career = careerDTO.toEntity()
        return careerRepository.save(career).toDTO()
    }

    private fun validate(careerDTO: CareerDTO) {
        if (careerDTO.name.isNullOrBlank()) {
            throw IllegalArgumentException("Career name cannot be null or blank")
        }
    }

    fun update(careerDTO: CareerDTO): CareerDTO {
        validate(careerDTO)
        val career = findEntityByPublicId(careerDTO.publicId)
        careerDTO.update(career)
        return careerRepository.save(career).toDTO()
    }
}