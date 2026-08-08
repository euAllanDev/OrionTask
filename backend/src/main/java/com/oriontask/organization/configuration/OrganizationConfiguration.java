package com.oriontask.organization.configuration;

import com.oriontask.organization.application.port.in.CreateOrganizationUseCase;
import com.oriontask.organization.application.port.out.OrganizationCreationAudit;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.application.usecase.CreateOrganizationService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OrganizationConfiguration {
  @Bean
  CreateOrganizationUseCase createOrganizationUseCase(
      OrganizationStore organizationStore, OrganizationCreationAudit audit, Clock clock) {
    return new CreateOrganizationService(organizationStore, audit, clock);
  }
}
