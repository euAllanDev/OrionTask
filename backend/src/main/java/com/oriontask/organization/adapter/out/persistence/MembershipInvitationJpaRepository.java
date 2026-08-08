package com.oriontask.organization.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MembershipInvitationJpaRepository
    extends JpaRepository<MembershipInvitationJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select invitation from MembershipInvitationJpaEntity invitation where invitation.organizationId = :organizationId and invitation.recipientAccountId = :accountId and invitation.status = 'PENDING'")
  Optional<MembershipInvitationJpaEntity> findPendingForUpdate(
      @Param("organizationId") UUID organizationId, @Param("accountId") UUID accountId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select invitation from MembershipInvitationJpaEntity invitation where invitation.tokenDerivation = :tokenDerivation")
  Optional<MembershipInvitationJpaEntity> findByTokenDerivationForUpdate(
      @Param("tokenDerivation") String tokenDerivation);
}
