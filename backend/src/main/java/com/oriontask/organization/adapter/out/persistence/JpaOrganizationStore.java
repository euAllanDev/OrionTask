package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.audit.application.port.out.AuditEventStore;
import com.oriontask.audit.domain.model.AuditAction;
import com.oriontask.audit.domain.model.AuditEvent;
import com.oriontask.organization.application.port.in.ListedOrganization;
import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import com.oriontask.organization.application.port.out.MembershipRevocationStore;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import com.oriontask.organization.domain.model.Organization;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaOrganizationStore
    implements OrganizationStore, MembershipInvitationStore, MembershipRevocationStore {
  private final OrganizationJpaRepository organizationRepository;
  private final MembershipJpaRepository membershipRepository;
  private final MembershipInvitationJpaRepository invitationRepository;
  private final AuditEventStore auditEventStore;

  JpaOrganizationStore(
      OrganizationJpaRepository organizationRepository,
      MembershipJpaRepository membershipRepository,
      MembershipInvitationJpaRepository invitationRepository,
      AuditEventStore auditEventStore) {
    this.organizationRepository = organizationRepository;
    this.membershipRepository = membershipRepository;
    this.invitationRepository = invitationRepository;
    this.auditEventStore = auditEventStore;
  }

  @Override
  @Transactional
  public void create(Organization organization, Membership membership) {
    organizationRepository.save(new OrganizationJpaEntity(organization));
    membershipRepository.saveAndFlush(new MembershipJpaEntity(membership));
    saveAudit(
        AuditAction.ORGANIZATION_CREATED,
        organization.id(),
        "organization",
        organization.id(),
        organization.createdAt(),
        membership.accountId());
  }

  @Override
  public Optional<Organization> findAuthorized(UUID organizationId, UUID accountId) {
    return organizationRepository
        .findAuthorized(organizationId, accountId)
        .map(OrganizationJpaEntity::toDomain);
  }

  @Override
  public List<ListedOrganization> findAllForAccount(UUID accountId) {
    return organizationRepository.findAllForAccount(accountId).stream()
        .map(
            organization ->
                new ListedOrganization(
                    organization.getId(),
                    organization.getName(),
                    organization.getCreatedAt(),
                    organization.getUpdatedAt(),
                    Membership.Role.valueOf(organization.getRole())))
        .toList();
  }

  @Override
  public Optional<Membership.Role> findMembershipRole(UUID organizationId, UUID accountId) {
    return membershipRepository
        .findByOrganizationIdAndAccountId(organizationId, accountId)
        .map(MembershipJpaEntity::role);
  }

  @Override
  @Transactional
  public boolean createIfEligible(
      MembershipInvitation invitation, UUID creatorAccountId, Instant now) {
    // Serialize invitation creation per organization before checking partial uniqueness.
    organizationRepository.findByIdForUpdate(invitation.organizationId()).orElseThrow();
    if (membershipRepository
        .findByOrganizationIdAndAccountId(
            invitation.organizationId(), invitation.recipientAccountId())
        .isPresent()) {
      return false;
    }
    Optional<MembershipInvitationJpaEntity> pending =
        invitationRepository.findPendingForUpdate(
            invitation.organizationId(), invitation.recipientAccountId());
    if (pending.isPresent()) {
      if (pending.get().expiresAt().isAfter(now)) {
        return false;
      }
      pending.get().expire();
      invitationRepository.flush();
    }
    invitationRepository.saveAndFlush(new MembershipInvitationJpaEntity(invitation));
    saveAudit(
        AuditAction.MEMBERSHIP_INVITATION_CREATED,
        invitation.organizationId(),
        "membership_invitation",
        invitation.id(),
        now,
        creatorAccountId);
    return true;
  }

  @Override
  @Transactional
  public Optional<AcceptedInvitation> accept(String tokenDerivation, UUID accountId, Instant now) {
    Optional<MembershipInvitationJpaEntity> found =
        invitationRepository.findByTokenDerivationForUpdate(tokenDerivation);
    if (found.isEmpty()) {
      return Optional.empty();
    }
    MembershipInvitationJpaEntity invitation = found.get();
    if (!invitation.recipientAccountId().equals(accountId)
        || invitation.status() != MembershipInvitation.Status.PENDING) {
      return Optional.empty();
    }
    if (!invitation.expiresAt().isAfter(now)) {
      invitation.expire();
      return Optional.empty();
    }
    invitation.accept(now);
    if (membershipRepository
        .findByOrganizationIdAndAccountId(invitation.organizationId(), accountId)
        .isPresent()) {
      return Optional.empty();
    }
    Membership membership =
        new Membership(
            UUID.randomUUID(), invitation.organizationId(), accountId, invitation.role(), now, now);
    membershipRepository.saveAndFlush(new MembershipJpaEntity(membership));
    saveAudit(
        AuditAction.MEMBERSHIP_INVITATION_ACCEPTED,
        invitation.organizationId(),
        "membership",
        membership.id(),
        now,
        accountId);
    return Optional.of(
        new AcceptedInvitation(
            invitation.id(),
            invitation.organizationId(),
            invitation.recipientAccountId(),
            membership));
  }

  @Override
  @Transactional
  public Result revoke(UUID organizationId, UUID actorAccountId, UUID revokedAccountId) {
    if (organizationRepository.findByIdForUpdate(organizationId).isEmpty()) {
      return Result.ACTOR_NOT_FOUND;
    }
    Optional<MembershipJpaEntity> actor =
        membershipRepository.findByOrganizationIdAndAccountId(organizationId, actorAccountId);
    if (actor.isEmpty()) {
      return Result.ACTOR_NOT_FOUND;
    }
    Optional<MembershipJpaEntity> target =
        membershipRepository.findByOrganizationIdAndAccountId(organizationId, revokedAccountId);
    if (target.isEmpty()) {
      return Result.TARGET_NOT_FOUND;
    }
    if (!actor.get().role().canRevoke(target.get().role())) {
      return Result.FORBIDDEN;
    }
    membershipRepository.delete(target.get());
    saveAudit(
        AuditAction.ORGANIZATION_MEMBERSHIP_REVOKED,
        organizationId,
        "membership",
        target.get().id(),
        Instant.now(),
        actorAccountId);
    return Result.REVOKED;
  }

  private void saveAudit(
      AuditAction action,
      UUID organizationId,
      String resourceType,
      UUID resourceId,
      Instant occurredAt) {
    saveAudit(action, organizationId, resourceType, resourceId, occurredAt, null);
  }

  private void saveAudit(
      AuditAction action,
      UUID organizationId,
      String resourceType,
      UUID resourceId,
      Instant occurredAt,
      UUID actorAccountId) {
    auditEventStore.save(
        new AuditEvent(
            UUID.randomUUID(),
            organizationId,
            actorAccountId == null ? resourceId : actorAccountId,
            action,
            resourceType,
            resourceId,
            occurredAt));
  }
}
