package ar.edu.uns.cs.thesisflow.auth.config

import ar.edu.uns.cs.thesisflow.auth.service.CustomUserDetailsService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers("/auth/professor/**").permitAll()
                    .requestMatchers("/analytics/**").permitAll()
                    .requestMatchers("/projects/public/**").permitAll()

                    // Authenticated endpoints
                    .requestMatchers("/auth/reset-password").authenticated()
                    .requestMatchers("/auth/me").authenticated()

                    // Read access for domain-related entities (PROFESSOR can read)
                    .requestMatchers(HttpMethod.GET, "/tags/**").hasAnyRole("ADMIN", "PROFESSOR")
                    .requestMatchers(HttpMethod.GET, "/careers/**").hasAnyRole("ADMIN", "PROFESSOR")
                    .requestMatchers(HttpMethod.GET, "/application-domains/**").hasAnyRole("ADMIN", "PROFESSOR")
                    .requestMatchers(HttpMethod.GET, "/students/**").hasAnyRole("ADMIN", "PROFESSOR")

                    // Write access for tags and domains (ADMIN + PROFESSOR - but filtered at service level)
                    .requestMatchers(HttpMethod.POST, "/tags").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/tags/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/tags/**").hasRole("ADMIN")
                    
                    .requestMatchers(HttpMethod.POST, "/careers").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/careers/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/careers/**").hasRole("ADMIN")
                    
                    .requestMatchers(HttpMethod.POST, "/application-domains").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/application-domains/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/application-domains/**").hasRole("ADMIN")

                    // Projects: PROFESSOR can perform ANY operation on projects they direct/co-direct
                    // (Authorization checked at service level via @PreAuthorize)
                    .requestMatchers("/projects/**").hasAnyRole("ADMIN", "PROFESSOR")

                    // Backup endpoints - ADMIN only
                    .requestMatchers("/backup/**").hasRole("ADMIN")

                    // Export endpoints - ADMIN only
                    .requestMatchers("/export/**").hasRole("ADMIN")

                    // Everything else requires ADMIN
                    .anyRequest().hasRole("ADMIN")
            }
            .userDetailsService(userDetailsService)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization")
            allowCredentials = true
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
