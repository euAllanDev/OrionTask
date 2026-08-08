package com.oriontask.organization.adapter.out.persistence;

import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaOrganizationStore implements OrganizationStore {
  private final OrganizationJpaRepository organizationRepository;
  private final MembershipJpaRepository membershipRepository;

  JpaOrganizationStore(
      OrganizationJpaRepository organizationRepository,
      MembershipJpaRepository membershipRepository) {
    this.organizationRepository = organizationRepository;
    this.membershipRepository = membershipRepository;
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
}
