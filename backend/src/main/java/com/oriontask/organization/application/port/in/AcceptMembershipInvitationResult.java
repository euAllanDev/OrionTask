package com.oriontask.organization.application.port.in;

import com.oriontask.organization.domain.model.Membership;
import java.util.UUID;

public record AcceptMembershipInvitationResult(
    UUID id, UUID organizationId, Membership.Role role) {}
