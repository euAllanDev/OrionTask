package com.oriontask.organization.configuration;

import com.oriontask.organization.application.port.in.AcceptMembershipInvitationUseCase;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationUseCase;
import com.oriontask.organization.application.port.in.CreateOrganizationUseCase;
import com.oriontask.organization.application.port.in.GetAuthorizedOrganizationUseCase;
import com.oriontask.organization.application.port.out.InvitationRecipientLookup;
import com.oriontask.organization.application.port.out.InvitationTokenDeriver;
import com.oriontask.organization.application.port.out.InvitationTokenGenerator;
import com.oriontask.organization.application.port.out.MembershipInvitationAudit;
import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import com.oriontask.organization.application.port.out.OrganizationCreationAudit;
import com.oriontask.organization.application.port.out.OrganizationStore;
import com.oriontask.organization.application.usecase.AcceptMembershipInvitationService;
import com.oriontask.organization.application.usecase.CreateMembershipInvitationService;
import com.oriontask.organization.application.usecase.CreateOrganizationService;
import com.oriontask.organization.application.usecase.GetAuthorizedOrganizationService;
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

  @Bean
  GetAuthorizedOrganizationUseCase getAuthorizedOrganizationUseCase(
      OrganizationStore organizationStore) {
    return new GetAuthorizedOrganizationService(organizationStore);
  }

  @Bean
  CreateMembershipInvitationUseCase createMembershipInvitationUseCase(
      MembershipInvitationStore store,
      InvitationRecipientLookup recipientLookup,
      InvitationTokenGenerator tokenGenerator,
      InvitationTokenDeriver tokenDeriver,
      MembershipInvitationAudit audit,
      Clock clock) {
    return new CreateMembershipInvitationService(
        store, recipientLookup, tokenGenerator, tokenDeriver, audit, clock);
  }

  @Bean
  AcceptMembershipInvitationUseCase acceptMembershipInvitationUseCase(
      MembershipInvitationStore store,
      InvitationTokenDeriver tokenDeriver,
      MembershipInvitationAudit audit,
      Clock clock) {
    return new AcceptMembershipInvitationService(store, tokenDeriver, audit, clock);
  }
}
