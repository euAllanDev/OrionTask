package com.oriontask.organization.application.port.out;

import java.util.UUID;

public interface OrganizationCreationAudit {
  void organizationCreated(UUID accountId, UUID organizationId);
}
