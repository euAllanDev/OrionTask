package com.oriontask.organization.application.usecase;

import com.oriontask.organization.application.port.in.RevokeMembershipCommand;
import com.oriontask.organization.application.port.in.RevokeMembershipUseCase;
import com.oriontask.organization.application.port.out.MembershipRevocationAudit;
import com.oriontask.organization.application.port.out.MembershipRevocationStore;

public class RevokeMembershipService implements RevokeMembershipUseCase {
  private final MembershipRevocationStore store;
  private final MembershipRevocationAudit audit;

  public RevokeMembershipService(MembershipRevocationStore store, MembershipRevocationAudit audit) {
    this.store = store;
    this.audit = audit;
  }

  @Override
  public void revoke(RevokeMembershipCommand command) {
    if (command.organizationId() == null
        || command.actorAccountId() == null
        || command.revokedAccountId() == null) {
      throw new IllegalArgumentException("Invalid membership revocation command");
    }
    switch (store.revoke(
        command.organizationId(), command.actorAccountId(), command.revokedAccountId())) {
      case REVOKED -> {
        try {
          audit.membershipRevoked(
              command.organizationId(), command.revokedAccountId(), command.actorAccountId());
        } catch (RuntimeException exception) {
          // Operational logging must not affect committed membership data.
        }
      }
      case ACTOR_NOT_FOUND, TARGET_NOT_FOUND -> throw new NotFoundException();
      case FORBIDDEN -> throw new ForbiddenException();
    }
  }

  public static class NotFoundException extends RuntimeException {}

  public static class ForbiddenException extends RuntimeException {}
}
