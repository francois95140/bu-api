package fr.esgi.bibliotheque.shared.config;

import fr.esgi.bibliotheque.shared.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class
SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publics
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/works/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Admin uniquement
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/api/policies/**").hasRole("ADMIN")
                        // Bibliothécaire et Admin
                        .requestMatchers("/api/users/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers("/api/penalties/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/works/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/works/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/works/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers("/api/copies/**").hasAnyRole("LIBRARIAN", "ADMIN")
                        // Circulation : emprunt/retour/perte gérés par le bibliothécaire
                        .requestMatchers(HttpMethod.POST, "/api/loans").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loans/return").hasAnyRole("LIBRARIAN", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/loans/*/lost").hasAnyRole("LIBRARIAN", "ADMIN")
                        // Réservation : retrait géré par le bibliothécaire
                        .requestMatchers(HttpMethod.POST, "/api/holds/*/pickup").hasAnyRole("LIBRARIAN", "ADMIN")
                        // Tout utilisateur authentifié (prolongation, réservation, consultation)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
