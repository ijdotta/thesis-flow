package ar.edu.uns.cs.thesisflow.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.web.config.EnableSpringDataWebSupport

/**
 * Enables a stable JSON representation for Spring Data Page responses.
 * This switches from the internal / implementation-dependent structure
 * to a DTO-based contract that is safe across framework upgrades.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
class PageSerializationConfig

