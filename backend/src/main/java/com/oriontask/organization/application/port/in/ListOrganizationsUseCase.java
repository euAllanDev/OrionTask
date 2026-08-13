package com.oriontask.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListOrganizationsUseCase {
  List<ListedOrganization> list(UUID accountId);
}
