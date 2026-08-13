package com.oriontask.organization.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.organization.application.port.in.RevokeMembershipCommand;
import com.oriontask.organization.application.port.out.MembershipRevocationAudit;
import com.oriontask.organization.application.port.out.MembershipRevocationStore;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RevokeMembershipServiceTest {
  @Test
  void revokesMembershipAndAuditsOnlyAfterStoreConfirmsRemoval() {
    UUID organizationId = UUID.randomUUID();
    UUID actorAccountId = UUID.randomUUID();
    UUID revokedAccountId = UUID.randomUUID();
    int[] audits = {0};
    MembershipRevocationAudit audit = (organization, revoked, actor) -> audits[0]++;
    var service =
        new RevokeMembershipService(
            (organization, actor, revoked) -> MembershipRevocationStore.Result.REVOKED, audit);

    service.revoke(new RevokeMembershipCommand(organizationId, actorAccountId, revokedAccountId));

    assertThat(audits[0]).isEqualTo(1);
  }

  @Test
  void hidesMissingActorAndTargetWithSameNotFoundException() {
    assertNotFound(MembershipRevocationStore.Result.ACTOR_NOT_FOUND);
    assertNotFound(MembershipRevocationStore.Result.TARGET_NOT_FOUND);
  }

  @Test
  void rejectsUnauthorizedRoleWithoutAuditing() {
    int[] audits = {0};
    var service =
        new RevokeMembershipService(
            (organization, actor, revoked) -> MembershipRevocationStore.Result.FORBIDDEN,
            (organization, revoked, actor) -> audits[0]++);

    assertThatThrownBy(() -> service.revoke(command()))
        .isInstanceOf(RevokeMembershipService.ForbiddenException.class);
    assertThat(audits[0]).isZero();
  }

  @Test
  void rejectsMissingIdentifiers() {
    var service =
        new RevokeMembershipService(
            (organization, actor, revoked) -> MembershipRevocationStore.Result.REVOKED,
            (organization, revoked, actor) -> {});

    assertThatThrownBy(
            () ->
                service.revoke(
                    new RevokeMembershipCommand(null, UUID.randomUUID(), UUID.randomUUID())))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static void assertNotFound(MembershipRevocationStore.Result result) {
    var service =
        new RevokeMembershipService(
            (organization, actor, revoked) -> result, (organization, revoked, actor) -> {});

    assertThatThrownBy(() -> service.revoke(command()))
        .isInstanceOf(RevokeMembershipService.NotFoundException.class);
  }

  private static RevokeMembershipCommand command() {
    return new RevokeMembershipCommand(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
  }
}
