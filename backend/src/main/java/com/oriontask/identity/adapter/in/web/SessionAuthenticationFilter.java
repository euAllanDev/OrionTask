package com.oriontask.identity.adapter.in.web;

import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.application.port.out.TokenDeriver;
import com.oriontask.identity.domain.model.AuthenticationSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
  static final String SESSION_COOKIE = "__Host-oriontask-session";
  private static final Duration ACTIVITY_UPDATE_INTERVAL = Duration.ofMinutes(5);
  private static final Duration INACTIVITY_TIMEOUT = Duration.ofMinutes(30);

  private final AuthenticationSessionStore sessionStore;
  private final TokenDeriver tokenDeriver;
  private final Clock clock;

  SessionAuthenticationFilter(
      AuthenticationSessionStore sessionStore, TokenDeriver tokenDeriver, Clock clock) {
    this.sessionStore = sessionStore;
    this.tokenDeriver = tokenDeriver;
    this.clock = clock;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = sessionToken(request.getCookies());
    if (token != null) {
      Instant now = Instant.now(clock);
      sessionStore
          .findByTokenDerivation(tokenDeriver.derive(token))
          .filter(session -> session.isActiveAt(now))
          .ifPresent(session -> authenticate(session, now));
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(AuthenticationSession session, Instant now) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
        new UsernamePasswordAuthenticationToken(session.accountId(), null, List.of()));
    SecurityContextHolder.setContext(context);
    if (!now.isBefore(session.lastActivityAt().plus(ACTIVITY_UPDATE_INTERVAL))) {
      sessionStore.updateLastActivityIfNeeded(
          session.id(), now, now.minus(ACTIVITY_UPDATE_INTERVAL), now.minus(INACTIVITY_TIMEOUT));
    }
  }

  static String sessionToken(Cookie[] cookies) {
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (SESSION_COOKIE.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
