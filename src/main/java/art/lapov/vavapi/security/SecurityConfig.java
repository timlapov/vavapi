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
        http.authorizeHttpRequests(request -> request
                // STATIONS:
                .requestMatchers(HttpMethod.POST, "/api/stations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/stations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/stations/**").authenticated()
                // LOCATIONS:
                .requestMatchers(HttpMethod.POST, "/api/locations").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/locations/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/locations/**").authenticated()
                // PRICING INTERVALS:
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/station/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/calculate-cost").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/pricing-intervals/check-availability").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pricing-intervals").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/pricing-intervals/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/pricing-intervals/**").authenticated()
                //
                .anyRequest().permitAll());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
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
