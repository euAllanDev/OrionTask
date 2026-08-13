package com.oriontask.audit.adapter.in.web;

import com.oriontask.audit.application.port.in.GetAuditEventsUseCase;
import com.oriontask.audit.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/audit-events")
class AuditEventController {
  private final GetAuditEventsUseCase useCase;

  AuditEventController(GetAuditEventsUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  ResponseEntity<List<AuditEventResponse>> get(
      @PathVariable String organizationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    if (organization == null || page < 0 || size < 1 || size > 100) {
      return ResponseEntity.badRequest().build();
    }
    try {
      GetAuditEventsUseCase.Result result = useCase.get(organization, accountId, page, size);
      if (result.status() == GetAuditEventsUseCase.Result.Status.ALLOWED) {
        return ResponseEntity.ok(result.events().stream().map(AuditEventResponse::from).toList());
      }
      return result.status() == GetAuditEventsUseCase.Result.Status.FORBIDDEN
          ? ResponseEntity.status(403).build()
          : ResponseEntity.notFound().build();
    } catch (ArithmeticException exception) {
      return ResponseEntity.badRequest().build();
    }
  }

  private static UUID uuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private record AuditEventResponse(
      UUID id,
      String action,
      String resourceType,
      UUID resourceId,
      UUID actorAccountId,
      Instant occurredAt) {
    static AuditEventResponse from(AuditEvent event) {
      return new AuditEventResponse(
          event.id(),
          event.action().value(),
          event.resourceType(),
          event.resourceId(),
          event.actorAccountId(),
          event.occurredAt());
    }
  }
}
