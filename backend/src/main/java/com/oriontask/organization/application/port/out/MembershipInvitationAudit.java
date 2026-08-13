package com.oriontask.organization.application.port.out;

import java.util.UUID;

public interface MembershipInvitationAudit {
  void invitationCreated(UUID invitationId, UUID organizationId, UUID recipientAccountId);

  void invitationAccepted(
      UUID invitationId, UUID organizationId, UUID recipientAccountId, UUID membershipId);
}
