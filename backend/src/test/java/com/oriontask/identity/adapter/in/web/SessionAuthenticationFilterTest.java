package com.oriontask.identity.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.application.port.out.TokenDeriver;
import com.oriontask.identity.domain.model.AuthenticationSession;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class SessionAuthenticationFilterTest {
  private static final Instant NOW = Instant.parse("2026-08-04T12:00:00Z");

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void updatesActivityOnlyAfterFiveMinutesAndUsesSessionAccountId() throws Exception {
    AuthenticationSessionStore sessionStore = mock(AuthenticationSessionStore.class);
    UUID accountId = UUID.randomUUID();
    AuthenticationSession session = session(accountId, NOW.minusSeconds(6 * 60));
    when(sessionStore.findByTokenDerivation("derived-token")).thenReturn(Optional.of(session));
    TokenDeriver deriver = token -> "derived-" + token;
    SessionAuthenticationFilter filter =
        new SessionAuthenticationFilter(sessionStore, deriver, Clock.fixed(NOW, ZoneOffset.UTC));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("__Host-oriontask-session", "token"));
    request.setParameter("accountId", UUID.randomUUID().toString());

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (ignoredRequest, ignoredResponse) ->
            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(accountId));

    verify(sessionStore)
        .updateLastActivityIfNeeded(
            eq(session.id()), eq(NOW), eq(NOW.minusSeconds(5 * 60)), eq(NOW.minusSeconds(30 * 60)));
  }

  @Test
  void doesNotUpdateActivityBeforeFiveMinutes() throws Exception {
    AuthenticationSessionStore sessionStore = mock(AuthenticationSessionStore.class);
    AuthenticationSession session = session(UUID.randomUUID(), NOW.minusSeconds(4 * 60));
    when(sessionStore.findByTokenDerivation("derived-token")).thenReturn(Optional.of(session));
    SessionAuthenticationFilter filter =
        new SessionAuthenticationFilter(
            sessionStore, token -> "derived-" + token, Clock.fixed(NOW, ZoneOffset.UTC));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new jakarta.servlet.http.Cookie("__Host-oriontask-session", "token"));

    filter.doFilter(
        request, new MockHttpServletResponse(), (ignoredRequest, ignoredResponse) -> {});

    verify(sessionStore, never()).updateLastActivityIfNeeded(any(), any(), any(), any());
  }

  private static AuthenticationSession session(UUID accountId, Instant lastActivityAt) {
    return new AuthenticationSession(
        UUID.randomUUID(),
        accountId,
        "derived-token",
        NOW.minusSeconds(60 * 60),
        lastActivityAt,
        NOW.plusSeconds(60 * 60),
        null);
  }
}
