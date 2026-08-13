package com.oriontask.ticket.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.application.port.out.TicketOpeningStore;
import com.oriontask.ticket.domain.model.Ticket;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OpenTicketServiceTest {
  @Test
  void defaultsPrioritySetsOpenAndTrimsTitle() {
    RecordingStore store = new RecordingStore();
    Ticket ticket = new OpenTicketService(store).open(command(" title ", null));

    assertThat(store.command.title()).isEqualTo("title");
    assertThat(store.command.priority()).isEqualTo(Ticket.Priority.MEDIUM);
    assertThat(ticket.status()).isEqualTo(Ticket.Status.OPEN);
  }

  @Test
  void rejectsInvalidTicketDataBeforePersistence() {
    RecordingStore store = new RecordingStore();
    assertThatThrownBy(() -> new OpenTicketService(store).open(command(" ", Ticket.Priority.HIGH)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(store.command).isNull();
  }

  @Test
  void mapsScopedValidationFailureToNotFound() {
    TicketOpeningStore store = (command, id) -> Optional.empty();
    assertThatThrownBy(
            () -> new OpenTicketService(store).open(command("title", Ticket.Priority.HIGH)))
        .isInstanceOf(OpenTicketService.NotFoundException.class);
  }

  private static OpenTicketCommand command(String title, Ticket.Priority priority) {
    return new OpenTicketCommand(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, title, null, priority);
  }

  private static class RecordingStore implements TicketOpeningStore {
    private OpenTicketCommand command;

    @Override
    public Optional<Ticket> open(OpenTicketCommand value, UUID id) {
      command = value;
      return Optional.of(
          new Ticket(
              id,
              value.organizationId(),
              value.customerId(),
              value.creatorAccountId(),
              value.assigneeAccountId(),
              value.title(),
              value.description(),
              value.priority(),
              Ticket.Status.OPEN,
              Instant.now(),
              Instant.now()));
    }
  }
}
