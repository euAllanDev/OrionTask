package com.oriontask.identity.application.usecase;

import com.oriontask.identity.application.port.in.CurrentSessionIdentity;
import com.oriontask.identity.application.port.in.GetCurrentSessionIdentityUseCase;
import com.oriontask.identity.application.port.out.AccountStore;
import java.util.Optional;
import java.util.UUID;

public class GetCurrentSessionIdentityService implements GetCurrentSessionIdentityUseCase {
  private final AccountStore accountStore;

  public GetCurrentSessionIdentityService(AccountStore accountStore) {
    this.accountStore = accountStore;
  }

  @Override
  public Optional<CurrentSessionIdentity> get(UUID accountId) {
    return accountStore
        .findById(accountId)
        .map(account -> new CurrentSessionIdentity(account.id(), account.normalizedEmail()));
  }
}
