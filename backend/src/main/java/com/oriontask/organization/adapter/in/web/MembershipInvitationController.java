package com.oriontask.organization.adapter.in.web;

import com.oriontask.organization.application.port.in.AcceptMembershipInvitationCommand;
import com.oriontask.organization.application.port.in.AcceptMembershipInvitationResult;
import com.oriontask.organization.application.port.in.AcceptMembershipInvitationUseCase;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationCommand;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationResult;
import com.oriontask.organization.application.port.in.CreateMembershipInvitationUseCase;
import com.oriontask.organization.application.usecase.CreateMembershipInvitationService.ForbiddenException;
import com.oriontask.organization.application.usecase.CreateMembershipInvitationService.NotFoundException;
import com.oriontask.organization.domain.model.Membership;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class MembershipInvitationController {
  private final CreateMembershipInvitationUseCase createUseCase;
  private final AcceptMembershipInvitationUseCase acceptUseCase;

  MembershipInvitationController(
      CreateMembershipInvitationUseCase createUseCase,
      AcceptMembershipInvitationUseCase acceptUseCase) {
    this.createUseCase = createUseCase;
    this.acceptUseCase = acceptUseCase;
  }

  @PostMapping("/organizations/{organizationId}/invitations")
  ResponseEntity<InvitationResponse> create(
      @PathVariable String organizationId,
      @RequestBody Map<String, Object> body,
      @AuthenticationPrincipal UUID accountId) {
    UUID parsedOrganizationId = parseUuid(organizationId);
    if (parsedOrganizationId == null
        || !body.keySet().equals(Set.of("recipientEmail", "role"))
        || !(body.get("recipientEmail") instanceof String email)
        || !(body.get("role") instanceof String role)) {
      return ResponseEntity.badRequest().build();
    }
    Membership.Role invitedRole;
    try {
      invitedRole = Membership.Role.valueOf(role);
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
    try {
      CreateMembershipInvitationResult result =
          createUseCase.create(
              new CreateMembershipInvitationCommand(
                  parsedOrganizationId, accountId, email, invitedRole));
      return ResponseEntity.accepted()
          .body(new InvitationResponse(result.token(), result.expiresAt()));
    } catch (NotFoundException exception) {
      return ResponseEntity.notFound().build();
    } catch (ForbiddenException exception) {
      return ResponseEntity.status(403).build();
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/membership-invitations/{token}/accept")
  ResponseEntity<MembershipResponse> accept(
      @PathVariable String token, @AuthenticationPrincipal UUID accountId) {
    Optional<AcceptMembershipInvitationResult> result =
        acceptUseCase.accept(new AcceptMembershipInvitationCommand(token, accountId));
    return result
        .<ResponseEntity<MembershipResponse>>map(
            value ->
                ResponseEntity.status(201)
                    .body(new MembershipResponse(value.id(), value.organizationId(), value.role())))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private static UUID parseUuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private record InvitationResponse(String token, Instant expiresAt) {}

  private record MembershipResponse(UUID id, UUID organizationId, Membership.Role role) {}
}
