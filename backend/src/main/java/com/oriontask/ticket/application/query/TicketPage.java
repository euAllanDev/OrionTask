package com.oriontask.ticket.application.query;

import com.oriontask.ticket.domain.model.Ticket;
import java.util.List;

public record TicketPage(List<Ticket> items, int page, int size, long totalElements) {
  public long totalPages() {
    return totalElements == 0 ? 0 : (totalElements + size - 1) / size;
  }
}
