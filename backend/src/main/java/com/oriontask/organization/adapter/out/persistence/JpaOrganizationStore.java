package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import com.oriontask.organization.application.port.out.MembershipRevocationStore;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import com.oriontask.organization.domain.model.Organization;
import java.time.Instant;
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

  JpaOrganizationStore(
      OrganizationJpaRepository organizationRepository,
      MembershipJpaRepository membershipRepository,
      MembershipInvitationJpaRepository invitationRepository) {
    this.organizationRepository = organizationRepository;
    this.membershipRepository = membershipRepository;
    this.invitationRepository = invitationRepository;
  }

  @Override
  @Transactional
  public void create(Organization organization, Membership membership) {
    organizationRepository.save(new OrganizationJpaEntity(organization));
    membershipRepository.saveAndFlush(new MembershipJpaEntity(membership));
  }

  @Override
  public Optional<Organization> findAuthorized(UUID organizationId, UUID accountId) {
    return organizationRepository
        .findAuthorized(organizationId, accountId)
        .map(OrganizationJpaEntity::toDomain);
  }

  @Override
  public Optional<Membership.Role> findMembershipRole(UUID organizationId, UUID accountId) {
    return membershipRepository
        .findByOrganizationIdAndAccountId(organizationId, accountId)
        .map(MembershipJpaEntity::role);
  }

  @Override
  @Transactional
  public boolean createIfEligible(MembershipInvitation invitation, Instant now) {
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
    return Result.REVOKED;
  }
}
