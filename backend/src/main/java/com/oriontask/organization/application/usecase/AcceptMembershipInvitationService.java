package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.AcceptMembershipInvitationCommand;
import com.oriontask.organization.application.port.in.AcceptMembershipInvitationResult;
import com.oriontask.organization.application.port.in.AcceptMembershipInvitationUseCase;
import com.oriontask.organization.application.port.out.InvitationTokenDeriver;
import com.oriontask.organization.application.port.out.MembershipInvitationAudit;
import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class AcceptMembershipInvitationService implements AcceptMembershipInvitationUseCase {
  private final MembershipInvitationStore store;
  private final InvitationTokenDeriver tokenDeriver;
  private final MembershipInvitationAudit audit;
  private final Clock clock;

  public AcceptMembershipInvitationService(
      MembershipInvitationStore store,
      InvitationTokenDeriver tokenDeriver,
      MembershipInvitationAudit audit,
      Clock clock) {
    this.store = store;
    this.tokenDeriver = tokenDeriver;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public Optional<AcceptMembershipInvitationResult> accept(
      AcceptMembershipInvitationCommand command) {
    if (command.accountId() == null || command.token() == null || command.token().isBlank()) {
      return Optional.empty();
    }
    return store
        .accept(tokenDeriver.derive(command.token()), command.accountId(), Instant.now(clock))
        .map(
            accepted -> {
              try {
                audit.invitationAccepted(
                    accepted.invitationId(),
                    accepted.organizationId(),
                    accepted.recipientAccountId(),
                    accepted.membership().id());
              } catch (RuntimeException exception) {
                // Operational logging must not affect committed membership data.
              }
              return new AcceptMembershipInvitationResult(
                  accepted.membership().id(),
                  accepted.organizationId(),
                  accepted.membership().role());
            });
  }
}
