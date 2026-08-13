package com.oriontask.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.in.RegistrationCommand;
import com.oriontask.identity.application.port.in.RegistrationResult;
import com.oriontask.identity.application.port.out.AccountRegistrationAudit;
import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.application.port.out.PasswordHasher;
import com.oriontask.identity.application.port.out.RegistrationRateLimiter;
import com.oriontask.identity.domain.model.Account;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegisterAccountServiceTest {
  @Test
  void normalizesEmailAndStoresOnlyThePasswordHash() {
    List<Account> accounts = new ArrayList<>();
    AccountStore store =
        account -> {
          accounts.add(account);
          return true;
        };
    PasswordHasher hasher = password -> "argon2:" + password.hashCode();
    RegistrationRateLimiter limiter = (sourceIp, normalizedEmail) -> true;
    AccountRegistrationAudit audit = accountId -> {};
    Clock clock = Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"), ZoneOffset.UTC);
    RegisterAccountService service =
        new RegisterAccountService(store, hasher, limiter, audit, clock);

    RegistrationResult result =
        service.register(
            new RegistrationCommand(" User@Example.com ", "long-password", "127.0.0.1"));

    assertThat(result).isEqualTo(RegistrationResult.ACCEPTED);
    assertThat(accounts)
        .singleElement()
        .satisfies(
            account -> {
              assertThat(account.normalizedEmail()).isEqualTo("user@example.com");
              assertThat(account.passwordHash())
                  .startsWith("argon2:")
                  .doesNotContain("long-password");
              assertThat(account.createdAt()).isEqualTo(Instant.parse("2026-08-02T00:00:00Z"));
            });
  }

  @Test
  void rejectsAnInvalidPasswordBeforeCallingAdapters() {
    AccountStore store = account -> true;
    PasswordHasher hasher = password -> "unused";
    RegistrationRateLimiter limiter = (sourceIp, normalizedEmail) -> true;
    AccountRegistrationAudit audit = accountId -> {};
    RegisterAccountService service =
        new RegisterAccountService(store, hasher, limiter, audit, Clock.systemUTC());

    RegistrationResult result =
        service.register(new RegistrationCommand("user@example.com", "short", "127.0.0.1"));

    assertThat(result).isEqualTo(RegistrationResult.INVALID);
  }
}
