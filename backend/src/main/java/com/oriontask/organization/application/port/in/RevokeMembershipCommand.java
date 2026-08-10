package com.oriontask.organization.application.port.in;

import java.util.UUID;

public record RevokeMembershipCommand(
    UUID organizationId, UUID actorAccountId, UUID revokedAccountId) {}
