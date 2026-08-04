package com.oriontask.identity.adapter.in.web;

import com.oriontask.identity.application.port.in.AuthenticateAccountUseCase;
import com.oriontask.identity.application.port.in.AuthenticationCommand;
import com.oriontask.identity.application.port.in.AuthenticationResult;
import com.oriontask.identity.application.port.in.LogoutCurrentSessionUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class SessionAuthenticationController {
  private final AuthenticateAccountUseCase authenticateAccountUseCase;
  private final LogoutCurrentSessionUseCase logoutCurrentSessionUseCase;
  private final CsrfTokenRepository csrfTokenRepository;
  private final TrustedSourceIpResolver sourceIpResolver;

  SessionAuthenticationController(
      AuthenticateAccountUseCase authenticateAccountUseCase,
      LogoutCurrentSessionUseCase logoutCurrentSessionUseCase,
      CsrfTokenRepository csrfTokenRepository,
      TrustedSourceIpResolver sourceIpResolver) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.logoutCurrentSessionUseCase = logoutCurrentSessionUseCase;
    this.csrfTokenRepository = csrfTokenRepository;
    this.sourceIpResolver = sourceIpResolver;
  }

  @PostMapping("/sessions")
  ResponseEntity<Map<String, String>> login(
      @RequestBody Map<String, Object> body,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (!body.keySet().equals(Set.of("email", "password"))
        || !(body.get("email") instanceof String email)
        || !(body.get("password") instanceof String password)) {
      return invalidCredentials();
    }
    AuthenticationResult result =
        authenticateAccountUseCase.authenticate(
            new AuthenticationCommand(email, password, sourceIpResolver.resolve(request)));
    if (result.status() == AuthenticationResult.Status.RATE_LIMITED) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .header(HttpHeaders.RETRY_AFTER, "900")
          .build();
    }
    if (result.status() == AuthenticationResult.Status.INVALID_CREDENTIALS) {
      return invalidCredentials();
    }

    csrfTokenRepository.saveToken(null, request, response);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, sessionCookie(result.sessionToken()))
        .build();
  }

  @DeleteMapping("/session")
  ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    logoutCurrentSessionUseCase.logout(
        SessionAuthenticationFilter.sessionToken(request.getCookies()));
    csrfTokenRepository.saveToken(null, request, response);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, expiredSessionCookie())
        .build();
  }

  private static ResponseEntity<Map<String, String>> invalidCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("message", "E-mail ou senha inválidos."));
  }

  private static String sessionCookie(String token) {
    return ResponseCookie.from(SessionAuthenticationFilter.SESSION_COOKIE, token)
        .secure(true)
        .httpOnly(true)
        .sameSite("Lax")
        .path("/")
        .build()
        .toString();
  }

  private static String expiredSessionCookie() {
    return ResponseCookie.from(SessionAuthenticationFilter.SESSION_COOKIE, "")
        .secure(true)
        .httpOnly(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ZERO)
        .build()
        .toString();
  }
}
