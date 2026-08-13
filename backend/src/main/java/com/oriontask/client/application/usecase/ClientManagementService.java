package com.oriontask.client.application.usecase;

import com.oriontask.client.application.port.in.ClientManagementUseCase;
import com.oriontask.client.application.port.out.ClientAudit;
import com.oriontask.client.application.port.out.ClientStore;
import com.oriontask.client.domain.model.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClientManagementService implements ClientManagementUseCase {
  private final ClientStore store;
  private final ClientAudit audit;

  public ClientManagementService(ClientStore store, ClientAudit audit) {
    this.store = store;
    this.audit = audit;
  }

  @Override
  public void create(UUID organizationId, UUID accountId, String name) {
    UUID clientId = UUID.randomUUID();
    mutate(store.create(organizationId, accountId, clientId, validName(name)));
    safeAudit(() -> audit.created(organizationId, clientId, accountId));
  }

  @Override
  public Optional<Client> find(UUID organizationId, UUID accountId, UUID clientId) {
    requireIds(organizationId, accountId, clientId);
    return store.find(organizationId, accountId, clientId);
  }

  @Override
  public List<Client> list(UUID organizationId, UUID accountId, Client.Status status) {
    if (organizationId == null || accountId == null) {
      throw new IllegalArgumentException("Invalid client query");
    }
    if (!store.hasAccess(organizationId, accountId)) {
      throw new NotFoundException();
    }
    return store.list(organizationId, accountId, status);
  }

  @Override
  public void update(UUID organizationId, UUID accountId, UUID clientId, String name) {
    requireIds(organizationId, accountId, clientId);
    mutate(store.update(organizationId, accountId, clientId, validName(name)));
    safeAudit(() -> audit.updated(organizationId, clientId, accountId));
  }

  @Override
  public void deactivate(UUID organizationId, UUID accountId, UUID clientId) {
    requireIds(organizationId, accountId, clientId);
    mutate(store.deactivate(organizationId, accountId, clientId));
    safeAudit(() -> audit.deactivated(organizationId, clientId, accountId));
  }

  private static void mutate(ClientStore.Result result) {
    if (result == ClientStore.Result.NOT_FOUND) {
      throw new NotFoundException();
    }
    if (result == ClientStore.Result.FORBIDDEN) {
      throw new ForbiddenException();
    }
  }

  private static String validName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Invalid client name");
    }
    String normalized = name.trim();
    if (normalized.isEmpty() || normalized.length() > 120) {
      throw new IllegalArgumentException("Invalid client name");
    }
    return normalized;
  }

  private static void requireIds(UUID organizationId, UUID accountId, UUID clientId) {
    if (organizationId == null || accountId == null || clientId == null) {
      throw new IllegalArgumentException("Invalid client command");
    }
  }

  private static void safeAudit(Runnable operation) {
    try {
      operation.run();
    } catch (RuntimeException exception) {
    }
  }

  public static class NotFoundException extends RuntimeException {}

  public static class ForbiddenException extends RuntimeException {}
}
