package org.doi.prmv4p113603.mlops.security;

import org.doi.prmv4p113603.mlops.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        /*
         * NOTE: CORS (Cross-Origin Resource Sharing) is a security feature implemented
         *  by web browsers to control how web pages can make requests to a different
         *  origin (domain, protocol, or port) than the one from which the page was served.
         *
         * NOTE: By default, for security reasons, browsers block cross-origin HTTP requests
         *  made from scripts (e.g., JavaScript in the Angular frontend). This is to prevent
         * Cross-Site Request Forgery (CSRF) and data leaks.
         *
         * NOTE: Bellow is an example of method reference; to the 'disable()' method
         *  of the 'AbstractHttpConfigurer' class. It works as a shorthand when the
         *  lambda simply calls one method on the input argument. The equivalent
         *  lambda expression is 'csrf -> csrf.disable()', which is explicitly
         *  declaring a parameter 'csrf' and calling 'disable()' on it.
         */
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.addAllowedOrigin("http://localhost:4200"); // CORS setup for the Angular front-end
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable) // CSRF disabled (since this is a stateless JWT auth)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll() // REST API endpoint "/auth/login" is explicitly permitted
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        // TODO: for now, in-memory users (user / password, admin / admin)
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER").build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN").build();

        return new InMemoryUserDetailsManager(user, admin);

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
