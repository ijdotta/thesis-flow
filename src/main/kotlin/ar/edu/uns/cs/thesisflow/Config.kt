package ar.edu.uns.cs.thesisflow

import ar.edu.uns.cs.thesisflow.config.CorsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class Config {

    @Bean
    fun corsConfigurer(corsProperties: CorsProperties): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
                    .allowedMethods(*corsProperties.allowedMethods.toTypedArray())
                    .allowedHeaders("*")
                    .exposedHeaders("*")
                    .allowCredentials(corsProperties.allowCredentials)
            }
        }
    }
}