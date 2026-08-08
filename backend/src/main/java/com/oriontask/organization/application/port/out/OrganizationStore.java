package com.oriontask.organization.application.port.out;

import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationStore {
  void create(Organization organization, Membership membership);

  default Optional<Organization> findAuthorized(UUID organizationId, UUID accountId) {
    return Optional.empty();
  }
}
