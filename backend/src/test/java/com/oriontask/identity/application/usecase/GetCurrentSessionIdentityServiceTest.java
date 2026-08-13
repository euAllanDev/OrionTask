package com.oriontask.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.domain.model.Account;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetCurrentSessionIdentityServiceTest {
  @Test
  void returnsStoredIdentityForAuthenticatedAccountId() {
    UUID accountId = UUID.randomUUID();
    Account account = new Account(accountId, "user@example.com", "hash", Instant.now());
    GetCurrentSessionIdentityService service = new GetCurrentSessionIdentityService(store(account));

    assertThat(service.get(accountId))
        .contains(
            new com.oriontask.identity.application.port.in.CurrentSessionIdentity(
                accountId, "user@example.com"));
  }

  @Test
  void doesNotConfirmMissingAccount() {
    GetCurrentSessionIdentityService service = new GetCurrentSessionIdentityService(store(null));

    assertThat(service.get(UUID.randomUUID())).isEmpty();
  }

  private static AccountStore store(Account account) {
    return new AccountStore() {
      @Override
      public boolean createIfAbsent(Account candidate) {
        return false;
      }

      @Override
      public Optional<Account> findById(UUID accountId) {
        return Optional.ofNullable(account).filter(candidate -> candidate.id().equals(accountId));
      }
    };
  }
}
