package com.oriontask.organization.application.port.in;

import com.oriontask.organization.domain.model.Organization;
import java.util.Optional;
import java.util.UUID;

public interface GetAuthorizedOrganizationUseCase {
  Optional<Organization> get(UUID organizationId, UUID accountId);
}
