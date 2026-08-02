package com.oriontask.identity.application.port.out;

import com.oriontask.identity.domain.model.Account;

public interface AccountStore {
  boolean createIfAbsent(Account account);
}
