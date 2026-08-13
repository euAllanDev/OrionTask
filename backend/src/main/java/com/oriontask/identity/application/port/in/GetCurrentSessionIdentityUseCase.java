package com.oriontask.identity.application.port.in;

import java.util.Optional;
import java.util.UUID;

public interface GetCurrentSessionIdentityUseCase {
  Optional<CurrentSessionIdentity> get(UUID accountId);
}
