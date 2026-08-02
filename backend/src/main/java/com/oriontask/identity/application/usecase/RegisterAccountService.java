package com.oriontask.identity.application.usecase;

import com.oriontask.identity.application.port.in.RegisterAccountUseCase;
import com.oriontask.identity.application.port.in.RegistrationCommand;
import com.oriontask.identity.application.port.in.RegistrationResult;
import com.oriontask.identity.application.port.out.AccountRegistrationAudit;
import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.identity.application.port.out.RegistrationRateLimiter;
import com.oriontask.identity.domain.model.Account;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public class RegisterAccountService implements RegisterAccountUseCase {
  private final AccountStore accountStore;
  private final PasswordHasher passwordHasher;
  private final RegistrationRateLimiter rateLimiter;
  private final AccountRegistrationAudit audit;
  private final Clock clock;

  public RegisterAccountService(
      AccountStore accountStore,
      PasswordHasher passwordHasher,
      RegistrationRateLimiter rateLimiter,
      AccountRegistrationAudit audit,
      Clock clock) {
    this.accountStore = accountStore;
    this.passwordHasher = passwordHasher;
    this.rateLimiter = rateLimiter;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public RegistrationResult register(RegistrationCommand command) {
    String normalizedEmail = normalizeEmail(command.email());
    if (normalizedEmail == null || !isValidPassword(command.password())) {
      return RegistrationResult.INVALID;
    }
    if (!rateLimiter.allow(command.sourceIp(), normalizedEmail)) {
      return RegistrationResult.RATE_LIMITED;
    }

    Account account =
        new Account(
            UUID.randomUUID(),
            normalizedEmail,
            passwordHasher.hash(command.password()),
            Instant.now(clock));
    if (accountStore.createIfAbsent(account)) {
      audit.accountCreated(account.id());
    }
    return RegistrationResult.ACCEPTED;
  }

  private static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (normalized.length() > 320 || !normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
      return null;
    }
    return normalized;
  }

  private static boolean isValidPassword(String password) {
    return password != null && password.length() >= 12 && password.length() <= 128;
  }
}
