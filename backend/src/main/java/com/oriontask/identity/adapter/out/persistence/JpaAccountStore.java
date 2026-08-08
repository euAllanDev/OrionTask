package com.oriontask.identity.adapter.out.persistence;

import com.oriontask.identity.application.port.out.AccountStore;
import com.oriontask.identity.domain.model.Account;
import com.oriontask.organization.application.port.out.InvitationRecipientLookup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class JpaAccountStore implements AccountStore, InvitationRecipientLookup {
  private final AccountJpaRepository repository;

  JpaAccountStore(AccountJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public boolean createIfAbsent(Account account) {
    if (repository.existsByNormalizedEmail(account.normalizedEmail())) {
      return false;
    }
    try {
      repository.saveAndFlush(
          new AccountJpaEntity(
              account.id(),
              account.normalizedEmail(),
              account.passwordHash(),
              account.createdAt()));
      return true;
    } catch (DataIntegrityViolationException exception) {
      return false;
    }
  }

  @Override
  public Optional<Account> findByNormalizedEmail(String normalizedEmail) {
    return repository.findByNormalizedEmail(normalizedEmail).map(AccountJpaEntity::toDomain);
  }

  @Override
  public Optional<UUID> findAccountIdByNormalizedEmail(String normalizedEmail) {
    return repository.findByNormalizedEmail(normalizedEmail).map(AccountJpaEntity::id);
  }
}
