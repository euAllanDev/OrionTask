package com.oriontask.organization.adapter.in.web;

import com.oriontask.organization.application.port.in.GetAuthorizedOrganizationUseCase;
import com.oriontask.organization.application.port.in.ListOrganizationsUseCase;
import com.oriontask.organization.application.port.in.ListedOrganization;
import com.oriontask.organization.domain.model.Organization;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationAccessController {
  private final GetAuthorizedOrganizationUseCase getAuthorizedOrganizationUseCase;
  private final ListOrganizationsUseCase listOrganizationsUseCase;

  OrganizationAccessController(
      GetAuthorizedOrganizationUseCase getAuthorizedOrganizationUseCase,
      ListOrganizationsUseCase listOrganizationsUseCase) {
    this.getAuthorizedOrganizationUseCase = getAuthorizedOrganizationUseCase;
    this.listOrganizationsUseCase = listOrganizationsUseCase;
  }

  @GetMapping
  ResponseEntity<List<ListedOrganization>> list(
      @RequestParam Map<String, String> query,
      @RequestBody(required = false) String body,
      @AuthenticationPrincipal UUID accountId) {
    if (!query.isEmpty() || body != null) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(listOrganizationsUseCase.list(accountId));
  }

  @GetMapping("/{organizationId}")
  ResponseEntity<OrganizationResponse> get(
      @PathVariable String organizationId, @AuthenticationPrincipal UUID accountId) {
    UUID parsedOrganizationId;
    try {
      parsedOrganizationId = UUID.fromString(organizationId);
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
    return getAuthorizedOrganizationUseCase
        .get(parsedOrganizationId, accountId)
        .map(OrganizationResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private record OrganizationResponse(UUID id, String name, Instant createdAt, Instant updatedAt) {
    static OrganizationResponse from(Organization organization) {
      return new OrganizationResponse(
          organization.id(),
          organization.name(),
          organization.createdAt(),
          organization.updatedAt());
    }
  }
}
