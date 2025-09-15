package ar.edu.uns.cs.thesisflow.common.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class ControllerExceptionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.info(ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(getErrorResponse(ex, request))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnauthorized(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error(ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(getErrorResponse(ex, request))
    }

    private fun getErrorResponse(ex: Exception, request: HttpServletRequest) = ErrorResponse(
        message = ex.localizedMessage,
        timestamp = Instant.now().toString(),
        path = request.requestURL.toString()
    )
}

data class ErrorResponse(
    val message: String,
    val timestamp: String,
    val path: String,
)