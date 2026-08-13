package com.oriontask.organization.application.port.in;

public interface CreateOrganizationUseCase {
  CreateOrganizationResult create(CreateOrganizationCommand command);
}
