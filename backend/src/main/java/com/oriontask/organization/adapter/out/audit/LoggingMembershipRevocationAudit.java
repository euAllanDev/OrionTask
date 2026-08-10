package com.oriontask.organization.adapter.out.audit;

import com.oriontask.organization.application.port.out.MembershipRevocationAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingMembershipRevocationAudit implements MembershipRevocationAudit {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingMembershipRevocationAudit.class);

  @Override
  public void membershipRevoked(
      UUID organizationId, UUID revokedAccountId, UUID revokedByAccountId) {
    LOGGER.info(
        "organization.membership_revoked organizationId={} revokedAccountId={} revokedByAccountId={}",
        organizationId,
        revokedAccountId,
        revokedByAccountId);
  }
}
