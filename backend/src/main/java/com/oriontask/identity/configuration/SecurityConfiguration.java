package com.oriontask.identity.configuration;

import com.oriontask.identity.adapter.in.web.SessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
class SecurityConfiguration {
  @Bean
  CsrfTokenRepository csrfTokenRepository() {
    return CookieCsrfTokenRepository.withHttpOnlyFalse();
  }

  @Bean
  AccessDeniedHandler csrfAccessDeniedHandler(CsrfTokenRepository csrfTokenRepository) {
    AccessDeniedHandler fallback = new AccessDeniedHandlerImpl();
    return (request, response, exception) -> {
      if (exception instanceof CsrfException) {
        csrfTokenRepository.saveToken(null, request, response);
      }
      fallback.handle(request, response, exception);
    };
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CsrfTokenRepository csrfTokenRepository,
      AccessDeniedHandler csrfAccessDeniedHandler,
      SessionAuthenticationFilter sessionAuthenticationFilter)
      throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(csrfTokenRepository)
                    .ignoringRequestMatchers("/api/v1/accounts"))
        .sessionManagement(
            sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> exception.accessDeniedHandler(csrfAccessDeniedHandler))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        "/actuator/health",
                        "/api/v1/accounts",
                        "/api/v1/csrf",
                        "/api/v1/sessions",
                        "/api/v1/session")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(sessionAuthenticationFilter, AnonymousAuthenticationFilter.class);
    return http.build();
  }
}
