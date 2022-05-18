package account.security;

import account.model.event.Action;
import account.model.user.Role;
import account.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsServiceImpl userDetailsService;
    private final EventService eventService;

    @Autowired
    public ApplicationSecurityConfig(UserDetailsServiceImpl userDetailsService, EventService eventService) {
        this.userDetailsService = userDetailsService;
        this.eventService = eventService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/api/auth/signup", "/actuator/shutdown").permitAll()
                .mvcMatchers("/api/security/**").hasRole(Role.AUDITOR.name())
                .mvcMatchers("/api/empl/payment").hasAnyRole(Role.ACCOUNTANT.name(), Role.USER.name())
                .mvcMatchers("/api/acct/payments").hasRole(Role.ACCOUNTANT.name())
                .mvcMatchers("/api/admin/**").hasRole(Role.ADMINISTRATOR.name())
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .httpBasic().authenticationEntryPoint(getAuthenticationEntryPoint())
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/h2-console/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(getPasswordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            eventService.saveLog(Action.ACCESS_DENIED,
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    request.getRequestURI(), request.getRequestURI());
            response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied!");
        };
    }

    @Bean
    AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return (request, response, authException) ->
        {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getOutputStream().println(new ObjectMapper().writeValueAsString(Map.of(
                    "timestamp", DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()),
                    "status", 401,
                    "error", "Unauthorized",
                    "message", "User account is locked",
                    "path", request.getRequestURI()
            )));
        };
    }
}