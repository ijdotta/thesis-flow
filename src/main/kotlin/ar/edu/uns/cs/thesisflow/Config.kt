package ar.edu.uns.cs.thesisflow

import ar.edu.uns.cs.thesisflow.common.CorsConfigConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class Config {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping(CorsConfigConstants.PATH_PATTERN)
                    .allowedOrigins(*CorsConfigConstants.ALLOWED_ORIGINS)
                    .allowedMethods(*CorsConfigConstants.ALLOWED_METHODS)
                    .allowedHeaders("*")
                    .exposedHeaders("*")
                    .allowCredentials(true)
            }
        }
    }
}