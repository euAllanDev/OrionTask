package com.oriontask.organization.application.port.out;

import java.util.UUID;

public interface MembershipRevocationStore {
  Result revoke(UUID organizationId, UUID actorAccountId, UUID revokedAccountId);

  enum Result {
    REVOKED,
    ACTOR_NOT_FOUND,
    TARGET_NOT_FOUND,
    FORBIDDEN
  }
}
