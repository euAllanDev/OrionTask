package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership_invitations")
class MembershipInvitationJpaEntity {
  @Id private UUID id;

  @Column(name = "organization_id", nullable = false)
  private UUID organizationId;

  @Column(name = "recipient_account_id", nullable = false)
  private UUID recipientAccountId;

  @Column(nullable = false)
  private String role;

  @Column(name = "token_derivation", nullable = false)
  private String tokenDerivation;

  @Column(nullable = false)
  private String status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "accepted_at")
  private Instant acceptedAt;

  protected MembershipInvitationJpaEntity() {}

  MembershipInvitationJpaEntity(MembershipInvitation invitation) {
    id = invitation.id();
    organizationId = invitation.organizationId();
    recipientAccountId = invitation.recipientAccountId();
    role = invitation.role().name();
    tokenDerivation = invitation.tokenDerivation();
    status = invitation.status().name();
    expiresAt = invitation.expiresAt();
    createdAt = invitation.createdAt();
    acceptedAt = invitation.acceptedAt();
  }

  MembershipInvitation toDomain() {
    return new MembershipInvitation(
        id,
        organizationId,
        recipientAccountId,
        Membership.Role.valueOf(role),
        tokenDerivation,
        MembershipInvitation.Status.valueOf(status),
        expiresAt,
        createdAt,
        acceptedAt);
  }

  void expire() {
    status = MembershipInvitation.Status.EXPIRED.name();
  }

  void accept(Instant now) {
    status = MembershipInvitation.Status.ACCEPTED.name();
    acceptedAt = now;
  }

  UUID id() {
    return id;
  }

  UUID organizationId() {
    return organizationId;
  }

  UUID recipientAccountId() {
    return recipientAccountId;
  }

  Membership.Role role() {
    return Membership.Role.valueOf(role);
  }

  MembershipInvitation.Status status() {
    return MembershipInvitation.Status.valueOf(status);
  }

  Instant expiresAt() {
    return expiresAt;
  }
}
