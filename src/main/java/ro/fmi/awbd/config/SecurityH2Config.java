package ro.fmi.awbd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ro.fmi.awbd.service.security.JpaUserDetailsService;

@Configuration
@Profile("test")
public class SecurityH2Config {

    private final JpaUserDetailsService userDetailsService;

    public SecurityH2Config(JpaUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/perform_login", "/logout", "/access_denied",
                                "/webjars/**", "/css/**", "/js/**", "/h2-console/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/*/new", "/*/*/edit",
                                "/shoots/*/media/**", "/shoots/*/invoice/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .loginProcessingUrl("/perform_login"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .rememberMe(remember -> remember
                        .key("awbd-remember-me-key")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .userDetailsService(userDetailsService))
                .exceptionHandling(ex -> ex.accessDeniedPage("/access_denied"))
                .build();
    }
}
