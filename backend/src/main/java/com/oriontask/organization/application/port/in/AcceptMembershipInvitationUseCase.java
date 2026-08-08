package com.oriontask.organization.application.port.in;

import java.util.Optional;

public interface AcceptMembershipInvitationUseCase {
  Optional<AcceptMembershipInvitationResult> accept(AcceptMembershipInvitationCommand command);
}
