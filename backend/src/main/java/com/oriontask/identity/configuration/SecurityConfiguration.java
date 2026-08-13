package com.oriontask.identity.configuration;

import com.oriontask.identity.adapter.in.web.SessionAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
class SecurityConfiguration {
  @Bean
  CsrfTokenRepository csrfTokenRepository() {
    HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    repository.setHeaderName("X-CSRF-TOKEN");
    return repository;
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
        .securityContext(
            context -> context.securityContextRepository(new NullSecurityContextRepository()))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exception ->
                exception
                    .accessDeniedHandler(csrfAccessDeniedHandler)
                    .authenticationEntryPoint(
                        (request, response, authenticationException) ->
                            response.setStatus(
                                request.getMethod().equals(HttpMethod.GET.name())
                                        && (request.getRequestURI().equals("/api/v1/session")
                                            || request
                                                .getRequestURI()
                                                .equals("/api/v1/organizations"))
                                    ? HttpStatus.UNAUTHORIZED.value()
                                    : HttpStatus.FORBIDDEN.value())))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        "/actuator/health", "/api/v1/accounts", "/api/v1/csrf", "/api/v1/sessions")
                    .permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/session")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(sessionAuthenticationFilter, AnonymousAuthenticationFilter.class);
    return http.build();
  }
}
