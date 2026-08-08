package com.oriontask.organization.adapter.out.audit;

import com.oriontask.organization.application.port.out.MembershipInvitationAudit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingMembershipInvitationAudit implements MembershipInvitationAudit {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(LoggingMembershipInvitationAudit.class);

  @Override
  public void invitationCreated(UUID invitationId, UUID organizationId, UUID recipientAccountId) {
    LOGGER.info(
        "membership_invitation.created invitationId={} organizationId={} recipientAccountId={}",
        invitationId,
        organizationId,
        recipientAccountId);
  }

  @Override
  public void invitationAccepted(
      UUID invitationId, UUID organizationId, UUID recipientAccountId, UUID membershipId) {
    LOGGER.info(
        "membership_invitation.accepted invitationId={} organizationId={} recipientAccountId={} membershipId={}",
        invitationId,
        organizationId,
        recipientAccountId,
        membershipId);
  }
}
