package com.oriontask.client.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.client.application.port.out.ClientStore;
import com.oriontask.client.domain.model.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClientManagementServiceTest {
  @Test
  void rejectsInvalidNameBeforePersistence() {
    var service = new ClientManagementService(new RecordingStore(), new RecordingAudit());
    assertThatThrownBy(() -> service.create(UUID.randomUUID(), UUID.randomUUID(), " "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void mapsUnauthorizedMutationToForbidden() {
    var service =
        new ClientManagementService(
            new RecordingStore(ClientStore.Result.FORBIDDEN), new RecordingAudit());
    assertThatThrownBy(() -> service.create(UUID.randomUUID(), UUID.randomUUID(), "Client"))
        .isInstanceOf(ClientManagementService.ForbiddenException.class);
  }

  private static class RecordingStore implements ClientStore {
    private final Result result;

    RecordingStore() {
      this(Result.CREATED);
    }

    RecordingStore(Result result) {
      this.result = result;
    }

    @Override
    public boolean hasAccess(UUID organizationId, UUID accountId) {
      return true;
    }

    @Override
    public Result create(UUID organizationId, UUID accountId, UUID clientId, String name) {
      return result;
    }

    @Override
    public Optional<Client> find(UUID organizationId, UUID accountId, UUID clientId) {
      return Optional.empty();
    }

    @Override
    public List<Client> list(UUID organizationId, UUID accountId, Client.Status status) {
      return List.of();
    }

    @Override
    public Result update(UUID organizationId, UUID accountId, UUID clientId, String name) {
      return result;
    }

    @Override
    public Result deactivate(UUID organizationId, UUID accountId, UUID clientId) {
      return result;
    }
  }

  private static class RecordingAudit
      implements com.oriontask.client.application.port.out.ClientAudit {
    @Override
    public void created(UUID organizationId, UUID clientId, UUID accountId) {}

    @Override
    public void updated(UUID organizationId, UUID clientId, UUID accountId) {}

    @Override
    public void deactivated(UUID organizationId, UUID clientId, UUID accountId) {}
  }
}
