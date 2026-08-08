package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.GetAuthorizedOrganizationUseCase;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Organization;
import java.util.Optional;
import java.util.UUID;

public class GetAuthorizedOrganizationService implements GetAuthorizedOrganizationUseCase {
  private final OrganizationStore organizationStore;

  public GetAuthorizedOrganizationService(OrganizationStore organizationStore) {
    this.organizationStore = organizationStore;
  }

  @Override
  public Optional<Organization> get(UUID organizationId, UUID accountId) {
    if (organizationId == null || accountId == null) {
      return Optional.empty();
    }
    return organizationStore.findAuthorized(organizationId, accountId);
  }
}
