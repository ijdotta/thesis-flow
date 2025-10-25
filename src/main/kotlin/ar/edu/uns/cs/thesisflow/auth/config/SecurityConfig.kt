package ar.edu.uns.cs.thesisflow.auth.config

import ar.edu.uns.cs.thesisflow.auth.service.CustomUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpMethod

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
    fun authenticationProvider(): DaoAuthenticationProvider = DaoAuthenticationProvider().apply {
        setUserDetailsService(userDetailsService)
        setPasswordEncoder(passwordEncoder())
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/projects/*/application-domain", "/projects/*/tags")
                    .hasAnyRole("ADMIN", "PROFESSOR")
                    .requestMatchers(HttpMethod.GET, "/projects/**")
                    .hasAnyRole("ADMIN", "PROFESSOR")
                    .anyRequest().hasRole("ADMIN")
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager
}
