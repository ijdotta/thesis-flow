package ar.edu.uns.cs.thesisflow

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class Config() {

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:5173") // exact origin, not "*"
                    .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("*")
                    .allowCredentials(true); // only if you need cookies
            }
        }
    }
}