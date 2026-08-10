package com.oriontask.client.application.port.in;

import com.oriontask.client.domain.model.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientManagementUseCase {
  void create(UUID organizationId, UUID accountId, String name);

  Optional<Client> find(UUID organizationId, UUID accountId, UUID clientId);

  List<Client> list(UUID organizationId, UUID accountId, Client.Status status);

  void update(UUID organizationId, UUID accountId, UUID clientId, String name);

  void deactivate(UUID organizationId, UUID accountId, UUID clientId);
}
