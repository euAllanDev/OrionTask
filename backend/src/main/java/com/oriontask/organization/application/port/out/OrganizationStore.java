package com.oriontask.organization.application.port.out;

import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.Organization;

public interface OrganizationStore {
  void create(Organization organization, Membership membership);
}
