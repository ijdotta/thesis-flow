package ar.edu.uns.cs.thesisflow.catalog.service

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.mapper.CareerMapper
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException
import ar.edu.uns.cs.thesisflow.common.exceptions.ValidationException

@Service
class CareerService(
    private val careerRepository: CareerRepository,
    private val careerMapper: CareerMapper,
) {
    fun findAll(pageable: Pageable): Page<CareerDTO> = careerRepository.findAll(pageable).map { careerMapper.toDto(it) }

    fun findByPublicId(publicId: String) = careerMapper.toDto(findEntityByPublicId(publicId))

    fun findEntityByPublicId(publicId: String?) = publicId?.let {
        careerRepository.findByPublicId(UUID.fromString(it))
    } ?: throw NotFoundException(ErrorMessages.careerNotFound(publicId))

    fun create(careerDTO: CareerDTO): CareerDTO {
        validate(careerDTO)
        val career = careerMapper.toEntity(careerDTO)
        return careerRepository.save(career).let { careerMapper.toDto(it) }
    }

    private fun validate(careerDTO: CareerDTO) {
        if (careerDTO.name.isNullOrBlank()) {
            throw ValidationException(ErrorMessages.careerNameBlank())
        }
    }

    fun update(careerDTO: CareerDTO): CareerDTO {
        validate(careerDTO)
        val career = findEntityByPublicId(careerDTO.publicId)
        careerMapper.updateEntityFromDto(careerDTO, career)
        return careerRepository.save(career).let { careerMapper.toDto(it) }
    }
}