package com.oriontask.organization.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.oriontask.organization.application.port.in.ListedOrganization;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListOrganizationsServiceTest {
  @Test
  void returnsMembershipRolesInFixedOrderForRequestedAccount() {
    UUID accountId = UUID.randomUUID();
    ListedOrganization older =
        organization("00000000-0000-0000-0000-000000000001", Instant.EPOCH, "OWNER");
    ListedOrganization laterId =
        organization("00000000-0000-0000-0000-000000000003", Instant.ofEpochSecond(1), "ADMIN");
    ListedOrganization earlierId =
        organization(
            "00000000-0000-0000-0000-000000000002", Instant.ofEpochSecond(1), "TECHNICIAN");
    OrganizationStore store =
        new OrganizationStore() {
          @Override
          public void create(Organization organization, Membership membership) {}

          @Override
          public List<ListedOrganization> findAllForAccount(UUID requestedAccountId) {
            return accountId.equals(requestedAccountId)
                ? List.of(older, earlierId, laterId)
                : List.of();
          }
        };

    List<ListedOrganization> organizations = new ListOrganizationsService(store).list(accountId);

    assertThat(organizations).containsExactly(laterId, earlierId, older);
    assertThat(organizations)
        .extracting(ListedOrganization::role)
        .containsExactly(Membership.Role.ADMIN, Membership.Role.TECHNICIAN, Membership.Role.OWNER);
    assertThat(new ListOrganizationsService(store).list(UUID.randomUUID())).isEmpty();
  }

  private static ListedOrganization organization(String id, Instant updatedAt, String role) {
    return new ListedOrganization(
        UUID.fromString(id),
        "Organization",
        Instant.EPOCH,
        updatedAt,
        Membership.Role.valueOf(role));
  }
}
