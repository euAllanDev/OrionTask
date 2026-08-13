package com.oriontask.identity.application.port.out;

import com.oriontask.identity.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

public interface AccountStore {
  boolean createIfAbsent(Account account);

  default Optional<Account> findByNormalizedEmail(String normalizedEmail) {
    return Optional.empty();
  }

  default Optional<Account> findById(UUID accountId) {
    return Optional.empty();
  }
}
