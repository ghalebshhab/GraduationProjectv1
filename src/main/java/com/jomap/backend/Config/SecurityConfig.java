package com.jomap.backend.Config;

import com.jomap.backend.Services.Auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;
        private final CustomUserDetailsService userDetailsService;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                        CustomUserDetailsService userDetailsService) {
                this.jwtAuthFilter = jwtAuthFilter;
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .cors(Customizer.withDefaults())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/api/auth/login").permitAll()
                                        .requestMatchers("/api/auth/register").permitAll()
                                        .requestMatchers("/api/auth/google").permitAll()
                                        .requestMatchers("/api/auth/facebook").permitAll()
                                                .requestMatchers("/api/admin/auth/**").permitAll()
                                                .requestMatchers("/api/test/**").authenticated()
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                        .requestMatchers("/api/auth/change-password").authenticated()
                                                .requestMatchers("/api/dev/**").permitAll()
                                                .requestMatchers("/api/governorates/**").permitAll()
                                                .requestMatchers("/api/activities/admin/**").hasAuthority("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/activities").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/activities/my").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/activities").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/activities/upcoming").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/activities/governorate/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/search/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/offers").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/offers/*/view").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/offers/*/click").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/support/tickets").permitAll()
                                                .anyRequest().authenticated())
                                .userDetailsService(userDetailsService)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(List.of(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://jomab-712232187160.europe-west1.run.app"
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }

}