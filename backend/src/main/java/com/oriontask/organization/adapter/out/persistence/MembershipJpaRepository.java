package com.oriontask.organization.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MembershipJpaRepository extends JpaRepository<MembershipJpaEntity, UUID> {
  Optional<MembershipJpaEntity> findByOrganizationIdAndAccountId(
      UUID organizationId, UUID accountId);
}
