package com.oriontask.organization.adapter.in.web;

import com.oriontask.organization.application.port.in.CreateOrganizationCommand;
import com.oriontask.organization.application.port.in.CreateOrganizationResult;
import com.oriontask.organization.application.port.in.CreateOrganizationUseCase;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationCreationController {
  private final CreateOrganizationUseCase createOrganizationUseCase;

  OrganizationCreationController(CreateOrganizationUseCase createOrganizationUseCase) {
    this.createOrganizationUseCase = createOrganizationUseCase;
  }

  @PostMapping
  ResponseEntity<OrganizationResponse> create(
      @RequestBody Map<String, Object> body, @AuthenticationPrincipal UUID accountId) {
    if (!body.keySet().equals(Set.of("name")) || !(body.get("name") instanceof String name)) {
      return ResponseEntity.badRequest().build();
    }

    try {
      CreateOrganizationResult result =
          createOrganizationUseCase.create(new CreateOrganizationCommand(accountId, name));
      return ResponseEntity.created(URI.create("/api/v1/organizations/" + result.id()))
          .body(new OrganizationResponse(result.id(), result.name(), result.createdAt()));
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
  }

  private record OrganizationResponse(UUID id, String name, Instant createdAt) {}
}
