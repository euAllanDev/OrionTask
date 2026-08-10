package com.oriontask.client.adapter.in.web;

import com.oriontask.client.application.port.in.ClientManagementUseCase;
import com.oriontask.client.application.usecase.ClientManagementService.ForbiddenException;
import com.oriontask.client.application.usecase.ClientManagementService.NotFoundException;
import com.oriontask.client.domain.model.Client;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/clients")
class ClientController {
  private final ClientManagementUseCase useCase;

  ClientController(ClientManagementUseCase useCase) {
    this.useCase = useCase;
  }

  @PostMapping
  ResponseEntity<Void> create(
      @PathVariable String organizationId,
      @RequestBody Map<String, Object> body,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    String name = name(body);
    if (organization == null || name == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      useCase.create(organization, accountId, name);
      return ResponseEntity.status(201)
          .location(java.net.URI.create("/api/v1/organizations/" + organization + "/clients"))
          .build();
    } catch (NotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (ForbiddenException e) {
      return ResponseEntity.status(403).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{clientId}")
  ResponseEntity<Client> find(
      @PathVariable String organizationId,
      @PathVariable String clientId,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId), client = uuid(clientId);
    if (organization == null || client == null) {
      return ResponseEntity.badRequest().build();
    }
    return useCase
        .find(organization, accountId, client)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  ResponseEntity<List<Client>> list(
      @PathVariable String organizationId,
      @RequestParam(defaultValue = "active") String status,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    if (organization == null
        || !(status.equals("active") || status.equals("inactive") || status.equals("all"))) {
      return ResponseEntity.badRequest().build();
    }
    Client.Status filter =
        status.equals("active")
            ? Client.Status.ACTIVE
            : status.equals("inactive") ? Client.Status.INACTIVE : null;
    try {
      return ResponseEntity.ok(useCase.list(organization, accountId, filter));
    } catch (NotFoundException exception) {
      return ResponseEntity.notFound().build();
    }
  }

  @PatchMapping("/{clientId}")
  ResponseEntity<Void> update(
      @PathVariable String organizationId,
      @PathVariable String clientId,
      @RequestBody Map<String, Object> body,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId), client = uuid(clientId);
    String name = name(body);
    if (organization == null || client == null || name == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      useCase.update(organization, accountId, client, name);
      return ResponseEntity.ok().build();
    } catch (NotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (ForbiddenException e) {
      return ResponseEntity.status(403).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{clientId}")
  ResponseEntity<Void> deactivate(
      @PathVariable String organizationId,
      @PathVariable String clientId,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId), client = uuid(clientId);
    if (organization == null || client == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      useCase.deactivate(organization, accountId, client);
      return ResponseEntity.noContent().build();
    } catch (NotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (ForbiddenException e) {
      return ResponseEntity.status(403).build();
    }
  }

  private static String name(Map<String, Object> body) {
    return body.keySet().equals(java.util.Set.of("name"))
            && body.get("name") instanceof String value
        ? value
        : null;
  }

  private static UUID uuid(String value) {
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
