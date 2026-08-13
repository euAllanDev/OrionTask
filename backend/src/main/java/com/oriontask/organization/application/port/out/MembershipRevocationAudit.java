package com.oriontask.organization.application.port.out;

import java.util.UUID;

public interface MembershipRevocationAudit {
  void membershipRevoked(UUID organizationId, UUID revokedAccountId, UUID revokedByAccountId);
}
