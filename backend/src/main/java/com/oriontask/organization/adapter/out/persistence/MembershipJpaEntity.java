package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.organization.domain.model.Membership;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization_memberships")
class MembershipJpaEntity {
  @Id private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private String role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected MembershipJpaEntity() {}

  MembershipJpaEntity(Membership membership) {
    id = membership.id();
    organizationId = membership.organizationId();
    accountId = membership.accountId();
    role = membership.role().name();
    createdAt = membership.createdAt();
    updatedAt = membership.updatedAt();
  }

  Membership.Role role() {
    return Membership.Role.valueOf(role);
  }
}
