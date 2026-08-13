package com.oriontask.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.in.AuthenticationCommand;
import com.oriontask.identity.application.port.in.AuthenticationResult;
import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.application.port.out.AuthenticationAudit;
import com.oriontask.identity.application.port.out.AuthenticationRateLimiter;
import com.oriontask.identity.application.port.out.AuthenticationSessionStore;
import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.identity.application.port.out.SessionTokenGenerator;
import com.oriontask.identity.application.port.out.TokenDeriver;
import com.oriontask.identity.domain.model.Account;
import com.oriontask.identity.domain.model.AuthenticationSession;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class AuthenticateAccountServiceTest {
  @Test
  void createsNewPersistedSessionForValidCredentials() {
    Account account =
        new Account(
            UUID.randomUUID(),
            "user@example.com",
            "stored-hash",
            Instant.parse("2026-08-01T00:00:00Z"));
    List<AuthenticationSession> sessions = new ArrayList<>();
    AuthenticateAccountService service =
        service(
            accountStore(email -> Optional.of(account)),
            new PasswordHasher() {
              @Override
              public String hash(String password) {
                return "unused";
              }

              @Override
              public boolean matches(String password, String passwordHash) {
                return password.equals("correct-password") && passwordHash.equals("stored-hash");
              }
            },
            sessions,
            AuthenticationRateLimiter.RateLimitDecision.ALLOWED);

    AuthenticationResult result =
        service.authenticate(
            new AuthenticationCommand(" User@Example.com ", "correct-password", "127.0.0.1"));

    assertThat(result.status()).isEqualTo(AuthenticationResult.Status.AUTHENTICATED);
    assertThat(result.sessionToken()).isEqualTo("new-session-token");
    assertThat(result.accountId()).isEqualTo(account.id());
    assertThat(sessions)
        .singleElement()
        .satisfies(
            session -> {
              assertThat(session.accountId()).isEqualTo(account.id());
              assertThat(session.tokenDerivation()).isEqualTo("derived:new-session-token");
              assertThat(session.absoluteExpiresAt())
                  .isEqualTo(Instant.parse("2026-08-02T08:00:00Z"));
            });
  }

  @Test
  void verifiesPreparedHashAndCreatesNoSessionForUnknownAccount() {
    List<AuthenticationSession> sessions = new ArrayList<>();
    boolean[] preparedHashUsed = {false};
    AuthenticateAccountService service =
        service(
            accountStore(email -> Optional.empty()),
            new PasswordHasher() {
              @Override
              public String hash(String password) {
                return "unused";
              }

              @Override
              public boolean matchesPreparedHash(String password) {
                preparedHashUsed[0] = true;
                return false;
              }
            },
            sessions,
            AuthenticationRateLimiter.RateLimitDecision.ALLOWED);

    AuthenticationResult result =
        service.authenticate(
            new AuthenticationCommand("unknown@example.com", "incorrect-password", "127.0.0.1"));

    assertThat(result.status()).isEqualTo(AuthenticationResult.Status.INVALID_CREDENTIALS);
    assertThat(preparedHashUsed[0]).isTrue();
    assertThat(sessions).isEmpty();
  }

  @Test
  void doesNotCreateSessionWhenOriginIsRateLimited() {
    List<AuthenticationSession> sessions = new ArrayList<>();
    AuthenticateAccountService service =
        service(
            accountStore(email -> Optional.empty()),
            password -> "unused",
            sessions,
            AuthenticationRateLimiter.RateLimitDecision.ORIGIN_BLOCKED);

    AuthenticationResult result =
        service.authenticate(
            new AuthenticationCommand("unknown@example.com", "incorrect-password", "127.0.0.1"));

    assertThat(result.status()).isEqualTo(AuthenticationResult.Status.RATE_LIMITED);
    assertThat(sessions).isEmpty();
  }

  private static AuthenticateAccountService service(
      AccountStore accountStore,
      PasswordHasher passwordHasher,
      List<AuthenticationSession> sessions,
      AuthenticationRateLimiter.RateLimitDecision decision) {
    AuthenticationSessionStore sessionStore =
        new AuthenticationSessionStore() {
          @Override
          public void create(AuthenticationSession session) {
            sessions.add(session);
          }

          @Override
          public Optional<AuthenticationSession> findByTokenDerivation(String tokenDerivation) {
            return Optional.empty();
          }

          @Override
          public void updateLastActivityIfNeeded(
              UUID sessionId, Instant now, Instant threshold, Instant inactivityCutoff) {}

          @Override
          public void revokeByTokenDerivation(String tokenDerivation, Instant now) {}
        };
    SessionTokenGenerator tokenGenerator = () -> "new-session-token";
    TokenDeriver tokenDeriver = value -> "derived:" + value;
    AuthenticationRateLimiter rateLimiter =
        new AuthenticationRateLimiter() {
          @Override
          public RateLimitDecision allowAttempt(String sourceIp, String protectedIdentifier) {
            return decision;
          }

          @Override
          public void recordFailure(String protectedIdentifier) {}

          @Override
          public void recordSuccess(String protectedIdentifier) {}
        };
    AuthenticationAudit audit =
        new AuthenticationAudit() {
          @Override
          public void loginSucceeded(UUID accountId) {}

          @Override
          public void loginRejected() {}

          @Override
          public void rateLimitApplied() {}

          @Override
          public void sessionLoggedOut() {}
        };
    return new AuthenticateAccountService(
        accountStore,
        passwordHasher,
        sessionStore,
        tokenGenerator,
        tokenDeriver,
        rateLimiter,
        audit,
        Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"), ZoneOffset.UTC));
  }

  private static AccountStore accountStore(Function<String, Optional<Account>> lookup) {
    return new AccountStore() {
      @Override
      public boolean createIfAbsent(Account account) {
        return false;
      }

      @Override
      public Optional<Account> findByNormalizedEmail(String normalizedEmail) {
        return lookup.apply(normalizedEmail);
      }
    };
  }
}
