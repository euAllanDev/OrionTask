package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.CreateOrganizationCommand;
import com.oriontask.organization.application.port.in.CreateOrganizationResult;
import com.oriontask.organization.application.port.in.CreateOrganizationUseCase;
import com.oriontask.organization.application.port.out.OrganizationCreationAudit;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class CreateOrganizationService implements CreateOrganizationUseCase {
  private final OrganizationStore organizationStore;
  private final OrganizationCreationAudit audit;
  private final Clock clock;

  public CreateOrganizationService(
      OrganizationStore organizationStore, OrganizationCreationAudit audit, Clock clock) {
    this.organizationStore = organizationStore;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public CreateOrganizationResult create(CreateOrganizationCommand command) {
    String name = normalizeName(command.name());
    if (command.accountId() == null || name == null) {
      throw new IllegalArgumentException("Invalid organization creation command");
    }

    Instant now = Instant.now(clock);
    UUID organizationId = UUID.randomUUID();
    Organization organization = new Organization(organizationId, name, now, now);
    Membership membership =
        new Membership(
            UUID.randomUUID(),
            organizationId,
            command.accountId(),
            Membership.Role.OWNER,
            now,
            now);
    organizationStore.create(organization, membership);
    try {
      audit.organizationCreated(command.accountId(), organizationId);
    } catch (RuntimeException exception) {
      // Operational evidence is best-effort and cannot undo committed business data.
    }
    return new CreateOrganizationResult(organizationId, name, now);
  }

  private static String normalizeName(String name) {
    if (name == null) {
      return null;
    }
    String normalized = name.trim();
    return normalized.isEmpty() || normalized.length() > 120 ? null : normalized;
  }
}
