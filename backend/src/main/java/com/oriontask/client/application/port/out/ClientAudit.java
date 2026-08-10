package com.oriontask.client.application.port.out;

import java.util.UUID;

public interface ClientAudit {
  void created(UUID organizationId, UUID clientId, UUID accountId);

  void updated(UUID organizationId, UUID clientId, UUID accountId);

  void deactivated(UUID organizationId, UUID clientId, UUID accountId);
}
