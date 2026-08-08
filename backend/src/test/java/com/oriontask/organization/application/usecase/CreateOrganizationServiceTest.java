package com.oriontask.organization.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.organization.application.port.in.CreateOrganizationCommand;
import com.oriontask.organization.application.port.out.OrganizationCreationAudit;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateOrganizationServiceTest {
  @Test
  void createsOwnerMembershipWithNormalizedName() {
    List<Organization> organizations = new ArrayList<>();
    List<Membership> memberships = new ArrayList<>();
    List<UUID> auditedOrganizationIds = new ArrayList<>();
    OrganizationStore store =
        (organization, membership) -> {
          organizations.add(organization);
          memberships.add(membership);
        };
    OrganizationCreationAudit audit =
        (accountId, organizationId) -> auditedOrganizationIds.add(organizationId);
    Clock clock = Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC);
    CreateOrganizationService service = new CreateOrganizationService(store, audit, clock);
    UUID accountId = UUID.randomUUID();

    var result = service.create(new CreateOrganizationCommand(accountId, "  Orion Support  "));

    assertThat(result.name()).isEqualTo("Orion Support");
    assertThat(organizations)
        .singleElement()
        .extracting(Organization::name)
        .isEqualTo("Orion Support");
    assertThat(memberships)
        .singleElement()
        .satisfies(
            membership -> {
              assertThat(membership.organizationId()).isEqualTo(result.id());
              assertThat(membership.accountId()).isEqualTo(accountId);
              assertThat(membership.role()).isEqualTo(Membership.Role.OWNER);
            });
    assertThat(auditedOrganizationIds).containsExactly(result.id());
  }

  @Test
  void rejectsInvalidNamesBeforeCallingAdapters() {
    OrganizationStore store = (organization, membership) -> {};
    OrganizationCreationAudit audit = (accountId, organizationId) -> {};
    CreateOrganizationService service =
        new CreateOrganizationService(store, audit, Clock.systemUTC());

    assertThatThrownBy(() -> service.create(new CreateOrganizationCommand(UUID.randomUUID(), "  ")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> service.create(new CreateOrganizationCommand(UUID.randomUUID(), "x".repeat(121))))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void keepsCreatedOrganizationWhenOperationalAuditFails() {
    List<Organization> organizations = new ArrayList<>();
    OrganizationStore store = (organization, membership) -> organizations.add(organization);
    OrganizationCreationAudit audit =
        (accountId, organizationId) -> {
          throw new IllegalStateException("Audit unavailable");
        };
    CreateOrganizationService service =
        new CreateOrganizationService(store, audit, Clock.systemUTC());

    var result = service.create(new CreateOrganizationCommand(UUID.randomUUID(), "Orion Support"));

    assertThat(result.id()).isEqualTo(organizations.getFirst().id());
  }
}
