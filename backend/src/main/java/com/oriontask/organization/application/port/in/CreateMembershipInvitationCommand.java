package com.oriontask.organization.application.port.in;

import com.oriontask.organization.domain.model.Membership;
import java.util.UUID;

public record CreateMembershipInvitationCommand(
    UUID organizationId, UUID creatorAccountId, String recipientEmail, Membership.Role role) {}
