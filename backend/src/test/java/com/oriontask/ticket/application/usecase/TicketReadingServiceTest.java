package com.oriontask.ticket.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oriontask.ticket.application.port.out.TicketReadingStore;
import com.oriontask.ticket.application.query.TicketListQuery;
import com.oriontask.ticket.application.query.TicketPage;
import com.oriontask.ticket.domain.model.Ticket;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TicketReadingServiceTest {
  @Test
  void rejectsInvalidPaginationBeforePersistence() {
    TicketReadingStore store =
        new TicketReadingStore() {
          @Override
          public Optional<Ticket> find(UUID organizationId, UUID ticketId, UUID accountId) {
            return Optional.empty();
          }

          @Override
          public Optional<TicketPage> list(TicketListQuery query) {
            throw new AssertionError("Store must not be called");
          }
        };

    assertThatThrownBy(() -> new TicketReadingService(store).list(query(-1, 50)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TicketReadingService(store).list(query(0, 101)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void preservesPageResultAndMapsMissingIdentityToNotFound() {
    TicketPage page = new TicketPage(List.of(), 0, 50, 0);
    TicketReadingStore store =
        new TicketReadingStore() {
          @Override
          public Optional<Ticket> find(UUID organizationId, UUID ticketId, UUID accountId) {
            return Optional.empty();
          }

          @Override
          public Optional<TicketPage> list(TicketListQuery query) {
            return Optional.of(page);
          }
        };
    TicketReadingService service = new TicketReadingService(store);

    assertThat(service.list(query(0, 50))).contains(page);
    assertThat(service.get(UUID.randomUUID(), UUID.randomUUID(), null)).isEmpty();
  }

  private static TicketListQuery query(int page, int size) {
    return new TicketListQuery(
        UUID.randomUUID(), UUID.randomUUID(), page, size, null, null, null, null);
  }
}
