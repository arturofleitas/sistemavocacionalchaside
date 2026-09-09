package com.tesis.vocacional.config;

import com.tesis.vocacional.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          LoginSuccessHandler loginSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/", "/registrarse", "/registro", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .requestMatchers("/test/publico/guardar").authenticated()
                .requestMatchers("/test/publico/**").permitAll()
                // El detalle de un reporte NO se protege por URL: el controlador aplica
                // el control de acceso centralizado (propiedad de la evaluación, rol y
                // sesión de invitado) devolviendo 403/404 sin filtrar datos. Se usa
                // AntPathRequestMatcher para que el patrón se evalúe sobre la ruta real.
                .requestMatchers(new AntPathRequestMatcher("/reportes-detalles/**")).permitAll()
                .requestMatchers("/usuarios/**", "/preguntas/**", "/gestion-test/**").hasRole("ADMIN")
                .requestMatchers("/realizar-test/**").hasAnyRole("ESTUDIANTE", "ADMIN")
                .requestMatchers("/reportes/**").hasAnyRole("ESTUDIANTE", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}