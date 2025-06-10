package ma.ensa.ebankingver1.config;

import ma.ensa.ebankingver1.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
<<<<<<< HEAD
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
=======
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import ma.ensa.ebankingver1.filter.JwtFilter;
<<<<<<< HEAD
@Import({RestAuthenticationEntryPoint.class,JwtFilter.class,
        CustomUserDetailsService.class})


@Configuration
@EnableWebSecurity
/*
public class SecurityConfig {

    public SecurityConfig() {
        System.out.println(">>> SecurityConfig is loaded");
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Qualifier("restAuthenticationEntryPoint") RestAuthenticationEntryPoint restAuthenticationEntryPoint, @Qualifier("jwtFilter") JwtFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http,
                                             @Qualifier("customUserDetailsService") CustomUserDetailsService customUserDetailsService,
                                             PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
        return authManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
}

 */
=======

@Import({RestAuthenticationEntryPoint.class, JwtFilter.class, CustomUserDetailsService.class})
@Configuration
@EnableWebSecurity
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
public class SecurityConfig {

    public SecurityConfig() {
        System.out.println(">>> SecurityConfig is loaded");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Qualifier("restAuthenticationEntryPoint") RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                                   @Qualifier("jwtFilter") JwtFilter jwtFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/verify-2fa", "/api/auth/resend-2fa").permitAll()
<<<<<<< HEAD

=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
                        .requestMatchers("/api/auth/role").authenticated() // Require authentication for /role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
<<<<<<< HEAD
                        .requestMatchers("/api/crypto/**").hasAnyRole("CLIENT", "ADMIN")
                        .requestMatchers("/api/recharge/**").hasAnyRole("CLIENT")

=======
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
                        .requestMatchers("/api/auth/validate-token/**").permitAll()
                        .requestMatchers("/api/auth/set-password").permitAll()
                        .requestMatchers("/api/auth/resend-activation").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:4200");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http,
<<<<<<< HEAD
                                             @Qualifier("customUserDetailsService") CustomUserDetailsService customUserDetailsService,
                                             PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
=======
                                             @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
                                             PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
        return authManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 11051e1e6c0c6b2d20e5f951fddd284d7ce5211a
