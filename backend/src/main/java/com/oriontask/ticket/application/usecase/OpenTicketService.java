package com.oriontask.ticket.application.usecase;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.application.port.in.OpenTicketUseCase;
import com.oriontask.ticket.application.port.out.TicketOpeningStore;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.UUID;

public class OpenTicketService implements OpenTicketUseCase {
  private final TicketOpeningStore store;

  public OpenTicketService(TicketOpeningStore store) {
    this.store = store;
  }

  @Override
  public Ticket open(OpenTicketCommand command) {
    OpenTicketCommand normalized = normalize(command);
    return store.open(normalized, UUID.randomUUID()).orElseThrow(NotFoundException::new);
  }

  private static OpenTicketCommand normalize(OpenTicketCommand command) {
    if (command == null
        || command.organizationId() == null
        || command.creatorAccountId() == null
        || command.customerId() == null
        || command.title() == null) {
      throw new IllegalArgumentException("Invalid ticket command");
    }
    String title = command.title().trim();
    if (title.isEmpty() || title.length() > 120) {
      throw new IllegalArgumentException("Invalid ticket title");
    }
    if (command.description() != null && command.description().length() > 4000) {
      throw new IllegalArgumentException("Invalid ticket description");
    }
    return new OpenTicketCommand(
        command.organizationId(),
        command.creatorAccountId(),
        command.customerId(),
        command.assigneeAccountId(),
        title,
        command.description(),
        command.priority() == null ? Ticket.Priority.MEDIUM : command.priority());
  }

  public static class NotFoundException extends RuntimeException {}
}
