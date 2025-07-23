package pl.eurokawa.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableMethodSecurity(jsr250Enabled = true)
@Configuration
public class SecurityConfiguration {
    private static final Logger log = LogManager.getLogger(SecurityConfiguration.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        log.info("myPrivate SecurityFilterChain starting properly");

        httpSecurity
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/vaadinServlet/UPLOAD/**",
                                "/",
                                "/api/upload",
                                "/login",
                                "/login/**",
                                "/register",
                                "/error",
                                "/webjars/**",
                                "/static/**",
                                "/VAADIN/**",
                                "/frontend/**",
                                "/webapp/**",
                                "/images/**",
                                "/icons/**",
                                "/apple-touch-icon-*.png",
                                "/favicon.ico",
                                "/sw.js",
                                "/sw-runtime-resources-precache.js",
                                "/receipts/**",
                                "/s3/**",
                                "/upload/**",
                                "/download/**",
                                "/delete/**",
                                "/users/*/emailConfirmation/**",
                                "/users/admin-account-confirmation/*/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home",true)
                        .successHandler(((request, response, authentication) -> {
                            log.info("Z a l o g o w a n o użytkownika: " + authentication.getName());
                            log.info("R o l a: " + authentication.getAuthorities() );
                            response.sendRedirect("/home");
                        }))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .csrf(csrf -> csrf.disable()
                );

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        AuthenticationManager manager = authenticationConfiguration.getAuthenticationManager();

        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
