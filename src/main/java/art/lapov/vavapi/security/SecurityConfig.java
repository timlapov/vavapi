package art.lapov.vavapi.security;

import art.lapov.vavapi.utils.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.authorizeHttpRequests(request -> request

                // ============= AUTHENTICATION & ACCOUNT =============
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/refresh-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/protected").authenticated()
                // Account management
                .requestMatchers(HttpMethod.POST, "/api/account").permitAll()  // Registration
                .requestMatchers(HttpMethod.GET, "/api/account/validate/**").permitAll()  // Email validation
                .requestMatchers(HttpMethod.POST, "/api/account/password/**").permitAll()  // Password reset request
                .requestMatchers(HttpMethod.GET, "/api/account/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/account/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/account/password").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/account/**").authenticated()

                // USERS (ADMIN ONLY):
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // ============= STATIONS =============
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/stations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stations/location/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/stations/available").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/stations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/stations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/stations/**").authenticated()

                // ============= LOCATIONS =============
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/locations").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/locations/search").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/locations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/locations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/locations/**").authenticated()

                // ============= PRICING INTERVALS =============
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/station/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/calculate-cost").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/check-availability").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/pricing-intervals").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/pricing-intervals/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/pricing-intervals/**").authenticated()

                // ============= RESERVATIONS =============
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/reservations/check-availability").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/my").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/upcoming").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/history").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/owner-history").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/pending-approval").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/station/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reservations/*/accept").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reservations/*/reject").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reservations/*/complete").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/reservations/*/pay").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/*/receipt.pdf").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/export/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").authenticated()

                // ============= REVIEWS =============
                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/reviews/reservation/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/station/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews/owner/**").permitAll()
                // Authenticated endpoints
                .requestMatchers(HttpMethod.POST, "/api/reviews/reservation/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reviews/my").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").authenticated()

                // ============= RATINGS =============
                .requestMatchers(HttpMethod.GET, "/api/ratings/**").permitAll()

                // ============= FILE UPLOADS =============
                .requestMatchers(HttpMethod.POST, "/api/files/**").authenticated()

                // ============= STATIC RESOURCES =============
                .requestMatchers("/uploads/**").permitAll()  // Public access to uploaded images
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()  // Static resources
                .requestMatchers("/favicon.ico").permitAll()
                .requestMatchers("/reset-password.html").permitAll()  // Password reset page

                // ============= API DOCUMENTATION =============
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()

                // ============= ACTUATOR (MONITORING) =============
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()  // Includes /liveness, /readiness
                .requestMatchers("/actuator/info").permitAll()

                .anyRequest().denyAll());
        
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ONLY FOR DEVELOPMENT
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // TODO FOR PRODUCTION
        // configuration.setAllowedOrigins(Arrays.asList("https://lapov.art"));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

//    @Bean
//    AuthenticationManager getManager() throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }

}
