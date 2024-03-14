package kapia.dev.security;

import jakarta.annotation.PostConstruct;
import kapia.dev.users.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    @Value("${su.username}")
    private String superUserUsername;

    @Value("${su.password}")
    private String superUserPassword;

//    @Bean
//    public UserDetailsService userDetailsService() {
//
//        UserDetails superUser = org.springframework.security.core.userdetails.User.withUsername(this.superUserUsername)
//                .password(this.passwordEncoder().encode(this.superUserPassword))
//                .roles("SUPERUSER")
//                .build();
//
//        return new InMemoryUserDetailsManager(superUser);
//    }

    // Use CustomUserDetailsService instead of InMemoryUserDetailsManager
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private WebApplicationContext applicationContext;

    @PostConstruct
    public void createSuperUser() {
        customUserDetailsService = applicationContext.getBean(CustomUserDetailsService.class);
        customUserDetailsService.createSuperUser(superUserUsername, superUserPassword);
    }

    @Bean
    public UserDetailsManager users(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
        auth.authenticationProvider(authenticationProvider());
        AuthenticationManager authenticationManager = auth.build();

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setAuthenticationManager(authenticationManager);
        return jdbcUserDetailsManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/getOCR").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").hasAnyRole("SUPERUSER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, AUTH_WHITELIST).permitAll()
                        .anyRequest().denyAll()
                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


}
