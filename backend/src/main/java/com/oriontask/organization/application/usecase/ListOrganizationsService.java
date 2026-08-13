package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.ListOrganizationsUseCase;
import com.oriontask.organization.application.port.in.ListedOrganization;
import com.oriontask.organization.application.port.out.OrganizationStore;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ListOrganizationsService implements ListOrganizationsUseCase {
  private static final Comparator<ListedOrganization> ORDER =
      Comparator.comparing(ListedOrganization::updatedAt)
          .reversed()
          .thenComparing(ListedOrganization::id, Comparator.reverseOrder());

  private final OrganizationStore organizationStore;

  public ListOrganizationsService(OrganizationStore organizationStore) {
    this.organizationStore = organizationStore;
  }

  @Override
  public List<ListedOrganization> list(UUID accountId) {
    if (accountId == null) {
      return List.of();
    }
    return organizationStore.findAllForAccount(accountId).stream().sorted(ORDER).toList();
  }
}
