package com.oriontask.organization.application.port.out;

import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MembershipInvitationStore {
  Optional<Membership.Role> findMembershipRole(UUID organizationId, UUID accountId);

  boolean createIfEligible(MembershipInvitation invitation, Instant now);

  Optional<AcceptedInvitation> accept(String tokenDerivation, UUID accountId, Instant now);

  record AcceptedInvitation(
      UUID invitationId, UUID organizationId, UUID recipientAccountId, Membership membership) {}
}
