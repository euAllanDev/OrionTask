package com.oriontask.organization.application.port.in;

import java.time.Instant;

public record CreateMembershipInvitationResult(String token, Instant expiresAt) {}
