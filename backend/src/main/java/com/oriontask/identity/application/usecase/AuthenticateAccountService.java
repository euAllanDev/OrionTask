package com.oriontask.identity.application.usecase;

import com.oriontask.identity.application.port.in.AuthenticateAccountUseCase;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class AuthenticateAccountService implements AuthenticateAccountUseCase {
  private static final Duration ABSOLUTE_DURATION = Duration.ofHours(8);

  private final AccountStore accountStore;
  private final PasswordHasher passwordHasher;
  private final AuthenticationSessionStore sessionStore;
  private final SessionTokenGenerator tokenGenerator;
  private final TokenDeriver tokenDeriver;
  private final AuthenticationRateLimiter rateLimiter;
  private final AuthenticationAudit audit;
  private final Clock clock;

  public AuthenticateAccountService(
      AccountStore accountStore,
      PasswordHasher passwordHasher,
      AuthenticationSessionStore sessionStore,
      SessionTokenGenerator tokenGenerator,
      TokenDeriver tokenDeriver,
      AuthenticationRateLimiter rateLimiter,
      AuthenticationAudit audit,
      Clock clock) {
    this.accountStore = accountStore;
    this.passwordHasher = passwordHasher;
    this.sessionStore = sessionStore;
    this.tokenGenerator = tokenGenerator;
    this.tokenDeriver = tokenDeriver;
    this.rateLimiter = rateLimiter;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public AuthenticationResult authenticate(AuthenticationCommand command) {
    String normalizedEmail = normalizeEmail(command.email());
    String protectedIdentifier =
        tokenDeriver.derive(normalizedEmail == null ? "" : normalizedEmail);
    AuthenticationRateLimiter.RateLimitDecision limitDecision =
        rateLimiter.allowAttempt(command.sourceIp(), protectedIdentifier);
    if (limitDecision == AuthenticationRateLimiter.RateLimitDecision.ORIGIN_BLOCKED) {
      audit.rateLimitApplied();
      return AuthenticationResult.rateLimited();
    }
    if (limitDecision == AuthenticationRateLimiter.RateLimitDecision.IDENTIFIER_BLOCKED) {
      audit.rateLimitApplied();
      return AuthenticationResult.invalidCredentials();
    }

    Optional<Account> account =
        normalizedEmail == null
            ? Optional.empty()
            : accountStore.findByNormalizedEmail(normalizedEmail);
    boolean passwordMatches =
        account
            .map(value -> passwordHasher.matches(command.password(), value.passwordHash()))
            .orElseGet(() -> passwordHasher.matchesPreparedHash(command.password()));
    if (account.isEmpty() || !passwordMatches) {
      rateLimiter.recordFailure(protectedIdentifier);
      audit.loginRejected();
      return AuthenticationResult.invalidCredentials();
    }

    Instant now = Instant.now(clock);
    String sessionToken = tokenGenerator.generate();
    sessionStore.create(
        new AuthenticationSession(
            UUID.randomUUID(),
            account.get().id(),
            tokenDeriver.derive(sessionToken),
            now,
            now,
            now.plus(ABSOLUTE_DURATION),
            null));
    rateLimiter.recordSuccess(protectedIdentifier);
    audit.loginSucceeded(account.get().id());
    return AuthenticationResult.authenticated(sessionToken, account.get().id());
  }

  private static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    return normalized.length() <= 320 && normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        ? normalized
        : null;
  }
}
