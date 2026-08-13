package com.oriontask.organization.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

interface OrganizationMembershipProjection {
  UUID getId();

  String getName();

  Instant getCreatedAt();

  Instant getUpdatedAt();

  String getRole();
}
