package com.oriontask.identity.application.port.out;

import java.util.UUID;

public interface AccountRegistrationAudit {
  void accountCreated(UUID accountId);
}
