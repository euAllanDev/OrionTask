package com.oriontask.organization.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.organization.application.port.in.AcceptMembershipInvitationCommand;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationCommand;
import com.oriontask.organization.application.port.out.InvitationRecipientLookup;
import com.oriontask.organization.application.port.out.InvitationTokenDeriver;
import com.oriontask.organization.application.port.out.InvitationTokenGenerator;
import com.oriontask.organization.application.port.out.MembershipInvitationAudit;
import com.oriontask.organization.application.port.out.MembershipInvitationStore;
import com.oriontask.organization.domain.model.Membership;
import com.oriontask.organization.domain.model.MembershipInvitation;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MembershipInvitationServiceTest {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-08-08T00:00:00Z"), ZoneOffset.UTC);

  @Test
  void ownerCreatesPersistedInvitationForExistingEligibleRecipient() {
    UUID organizationId = UUID.randomUUID();
    UUID recipientId = UUID.randomUUID();
    RecordingStore store = new RecordingStore(Membership.Role.OWNER);
    var service = createService(store, email -> Optional.of(recipientId));

    var result =
        service.create(
            new CreateMembershipInvitationCommand(
                organizationId,
                UUID.randomUUID(),
                " Recipient@example.com ",
                Membership.Role.ADMIN));

    assertThat(result.token()).hasSize(43);
    assertThat(result.expiresAt()).isEqualTo(Instant.parse("2026-08-15T00:00:00Z"));
    assertThat(store.created)
        .satisfies(
            invitation -> {
              assertThat(invitation.recipientAccountId()).isEqualTo(recipientId);
              assertThat(invitation.tokenDerivation()).isEqualTo("derived-" + "a".repeat(43));
              assertThat(invitation.status()).isEqualTo(MembershipInvitation.Status.PENDING);
            });
  }

  @Test
  void createsCoverageTokenWithoutPersistingWhenRecipientIsMissing() {
    RecordingStore store = new RecordingStore(Membership.Role.ADMIN);
    var result =
        createService(store, email -> Optional.empty())
            .create(
                new CreateMembershipInvitationCommand(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "missing@example.com",
                    Membership.Role.TECHNICIAN));
    assertThat(result.token()).hasSize(43);
    assertThat(store.created).isNull();
  }

  @Test
  void rejectsRoleOutsideCreatorAuthority() {
    RecordingStore store = new RecordingStore(Membership.Role.ADMIN);
    assertThatThrownBy(
            () ->
                createService(store, email -> Optional.of(UUID.randomUUID()))
                    .create(
                        new CreateMembershipInvitationCommand(
                            UUID.randomUUID(),
                            UUID.randomUUID(),
                            "recipient@example.com",
                            Membership.Role.ADMIN)))
        .isInstanceOf(CreateMembershipInvitationService.ForbiddenException.class);
  }

  @Test
  void acceptsOnlyStoreConfirmedInvitationAndAuditsMembershipCreation() {
    UUID invitationId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Membership membership =
        new Membership(
            UUID.randomUUID(),
            organizationId,
            accountId,
            Membership.Role.TECHNICIAN,
            Instant.now(CLOCK),
            Instant.now(CLOCK));
    MembershipInvitationStore store =
        new RecordingStore(Membership.Role.OWNER) {
          @Override
          public Optional<AcceptedInvitation> accept(
              String derivation, UUID recipient, Instant now) {
            return Optional.of(
                new AcceptedInvitation(invitationId, organizationId, accountId, membership));
          }
        };
    int[] audits = {0};
    MembershipInvitationAudit audit =
        new MembershipInvitationAudit() {
          @Override
          public void invitationCreated(UUID a, UUID b, UUID c) {}

          @Override
          public void invitationAccepted(UUID a, UUID b, UUID c, UUID d) {
            audits[0]++;
          }
        };
    var service =
        new AcceptMembershipInvitationService(store, value -> "derived-" + value, audit, CLOCK);
    assertThat(service.accept(new AcceptMembershipInvitationCommand("token", accountId)))
        .isPresent();
    assertThat(audits[0]).isEqualTo(1);
  }

  private static CreateMembershipInvitationService createService(
      RecordingStore store, InvitationRecipientLookup lookup) {
    InvitationTokenGenerator generator = () -> "a".repeat(43);
    InvitationTokenDeriver deriver = value -> "derived-" + value;
    MembershipInvitationAudit audit =
        new MembershipInvitationAudit() {
          @Override
          public void invitationCreated(UUID a, UUID b, UUID c) {}

          @Override
          public void invitationAccepted(UUID a, UUID b, UUID c, UUID d) {}
        };
    return new CreateMembershipInvitationService(store, lookup, generator, deriver, audit, CLOCK);
  }

  private static class RecordingStore implements MembershipInvitationStore {
    private final Membership.Role creatorRole;
    MembershipInvitation created;

    RecordingStore(Membership.Role creatorRole) {
      this.creatorRole = creatorRole;
    }

    @Override
    public Optional<Membership.Role> findMembershipRole(UUID organizationId, UUID accountId) {
      return Optional.of(creatorRole);
    }

    @Override
    public boolean createIfEligible(MembershipInvitation invitation, Instant now) {
      created = invitation;
      return true;
    }

    @Override
    public Optional<AcceptedInvitation> accept(String derivation, UUID accountId, Instant now) {
      return Optional.empty();
    }
  }
}
