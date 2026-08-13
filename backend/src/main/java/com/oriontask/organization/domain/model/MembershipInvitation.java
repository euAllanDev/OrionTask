package com.oriontask.organization.domain.model;

import java.time.Instant;
import java.util.UUID;

public record MembershipInvitation(
    UUID id,
    UUID organizationId,
    UUID recipientAccountId,
    Membership.Role role,
    String tokenDerivation,
    Status status,
    Instant expiresAt,
    Instant createdAt,
    Instant acceptedAt) {
  public MembershipInvitation {
    if (id == null
        || organizationId == null
        || recipientAccountId == null
        || role == null
        || tokenDerivation == null
        || status == null
        || expiresAt == null
        || createdAt == null) {
      throw new IllegalArgumentException("Invitation fields must be present");
    }
  }

  public enum Status {
    PENDING,
    ACCEPTED,
    EXPIRED
  }
}
