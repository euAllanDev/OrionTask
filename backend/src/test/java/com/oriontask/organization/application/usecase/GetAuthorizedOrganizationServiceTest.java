package com.oriontask.organization.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetAuthorizedOrganizationServiceTest {
  @Test
  void resolvesOnlyOrganizationMatchedToAuthenticatedAccount() {
    UUID organizationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Organization organization =
        new Organization(organizationId, "Orion Support", Instant.EPOCH, Instant.EPOCH);
    OrganizationStore store =
        new OrganizationStore() {
          @Override
          public void create(Organization ignored, Membership membership) {}

          @Override
          public Optional<Organization> findAuthorized(
              UUID requestedOrganizationId, UUID requestedAccountId) {
            return organizationId.equals(requestedOrganizationId)
                    && accountId.equals(requestedAccountId)
                ? Optional.of(organization)
                : Optional.empty();
          }
        };
    GetAuthorizedOrganizationService service = new GetAuthorizedOrganizationService(store);

    assertThat(service.get(organizationId, accountId)).contains(organization);
    assertThat(service.get(organizationId, UUID.randomUUID())).isEmpty();
    assertThat(service.get(UUID.randomUUID(), accountId)).isEmpty();
  }
}
