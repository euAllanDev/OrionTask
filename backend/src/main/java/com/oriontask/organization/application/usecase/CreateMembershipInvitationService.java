package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.CreateMembershipInvitationCommand;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationResult;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationUseCase;
import com.oriontask.organization.application.port.out.InvitationRecipientLookup;
import com.oriontask.organization.application.port.out.InvitationTokenDeriver;
import com.oriontask.organization.application.port.out.InvitationTokenGenerator;
import com.oriontask.organization.application.port.out.MembershipInvitationAudit;
import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CreateMembershipInvitationService implements CreateMembershipInvitationUseCase {
  private final MembershipInvitationStore store;
  private final InvitationRecipientLookup recipientLookup;
  private final InvitationTokenGenerator tokenGenerator;
  private final InvitationTokenDeriver tokenDeriver;
  private final MembershipInvitationAudit audit;
  private final Clock clock;

  public CreateMembershipInvitationService(
      MembershipInvitationStore store,
      InvitationRecipientLookup recipientLookup,
      InvitationTokenGenerator tokenGenerator,
      InvitationTokenDeriver tokenDeriver,
      MembershipInvitationAudit audit,
      Clock clock) {
    this.store = store;
    this.recipientLookup = recipientLookup;
    this.tokenGenerator = tokenGenerator;
    this.tokenDeriver = tokenDeriver;
    this.audit = audit;
    this.clock = clock;
  }

  @Override
  public CreateMembershipInvitationResult create(CreateMembershipInvitationCommand command) {
    String email = normalizeEmail(command.recipientEmail());
    if (command.organizationId() == null
        || command.creatorAccountId() == null
        || email == null
        || command.role() == null) {
      throw new IllegalArgumentException("Invalid membership invitation command");
    }
    Membership.Role creatorRole =
        store
            .findMembershipRole(command.organizationId(), command.creatorAccountId())
            .orElseThrow(NotFoundException::new);
    if (!canInvite(creatorRole, command.role())) {
      throw new ForbiddenException();
    }
    Instant now = Instant.now(clock);
    Instant expiresAt = now.plus(7, ChronoUnit.DAYS);
    String token = tokenGenerator.generate();
    recipientLookup
        .findAccountIdByNormalizedEmail(email)
        .ifPresent(
            recipientId -> {
              MembershipInvitation invitation =
                  new MembershipInvitation(
                      UUID.randomUUID(),
                      command.organizationId(),
                      recipientId,
                      command.role(),
                      tokenDeriver.derive(token),
                      MembershipInvitation.Status.PENDING,
                      expiresAt,
                      now,
                      null);
              if (store.createIfEligible(invitation, command.creatorAccountId(), now)) {
                try {
                  audit.invitationCreated(
                      invitation.id(),
                      invitation.organizationId(),
                      invitation.recipientAccountId());
                } catch (RuntimeException exception) {
                  // Operational logging must not affect committed invitation data.
                }
              }
            });
    return new CreateMembershipInvitationResult(token, expiresAt);
  }

  private static boolean canInvite(Membership.Role creator, Membership.Role invited) {
    return (creator == Membership.Role.OWNER
            && (invited == Membership.Role.ADMIN || invited == Membership.Role.TECHNICIAN))
        || (creator == Membership.Role.ADMIN && invited == Membership.Role.TECHNICIAN);
  }

  private static String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
    return normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ? normalized : null;
  }

  public static class NotFoundException extends RuntimeException {}

  public static class ForbiddenException extends RuntimeException {}
}
