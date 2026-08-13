package com.oriontask.organization.application.port.in;

public interface CreateMembershipInvitationUseCase {
  CreateMembershipInvitationResult create(CreateMembershipInvitationCommand command);
}
