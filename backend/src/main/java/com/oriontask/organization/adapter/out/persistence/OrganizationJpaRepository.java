package com.oriontask.organization.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select organization from OrganizationJpaEntity organization where organization.id = :organizationId")
  Optional<OrganizationJpaEntity> findByIdForUpdate(@Param("organizationId") UUID organizationId);

  @Query(
      """
      select organization from OrganizationJpaEntity organization
      where organization.id = :organizationId
      and exists (
        select 1 from MembershipJpaEntity membership
        where membership.organizationId = organization.id and membership.accountId = :accountId
      )
      """)
  Optional<OrganizationJpaEntity> findAuthorized(
      @Param("organizationId") UUID organizationId, @Param("accountId") UUID accountId);
}
