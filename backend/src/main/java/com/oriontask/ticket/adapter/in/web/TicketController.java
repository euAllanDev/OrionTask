package com.oriontask.ticket.adapter.in.web;

import com.oriontask.ticket.application.command.OpenTicketCommand;
import com.oriontask.ticket.application.port.in.OpenTicketUseCase;
import com.oriontask.ticket.application.port.in.TicketReadingUseCase;
import com.oriontask.ticket.application.query.TicketListQuery;
import com.oriontask.ticket.application.query.TicketPage;
import com.oriontask.ticket.application.usecase.OpenTicketService.NotFoundException;
import com.oriontask.ticket.domain.model.Ticket;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/tickets")
class TicketController {
  private static final Set<String> FIELDS =
      Set.of("title", "description", "customerId", "priority", "assigneeAccountId");
  private static final Set<String> LIST_PARAMETERS =
      Set.of("page", "size", "status", "priority", "customerId", "assigneeAccountId");
  private final OpenTicketUseCase openTicketUseCase;
  private final TicketReadingUseCase ticketReadingUseCase;

  TicketController(OpenTicketUseCase openTicketUseCase, TicketReadingUseCase ticketReadingUseCase) {
    this.openTicketUseCase = openTicketUseCase;
    this.ticketReadingUseCase = ticketReadingUseCase;
  }

  @PostMapping
  ResponseEntity<TicketResponse> open(
      @PathVariable String organizationId,
      @RequestBody(required = false) Map<String, Object> body,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    OpenTicketCommand command = command(organization, accountId, body);
    if (command == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      Ticket ticket = openTicketUseCase.open(command);
      return ResponseEntity.created(
              URI.create("/api/v1/organizations/" + organization + "/tickets/" + ticket.id()))
          .body(TicketResponse.from(ticket));
    } catch (NotFoundException exception) {
      return ResponseEntity.notFound().build();
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/{ticketId}")
  ResponseEntity<TicketDetailResponse> get(
      @PathVariable String organizationId,
      @PathVariable String ticketId,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    UUID ticket = uuid(ticketId);
    if (organization == null || ticket == null) {
      return ResponseEntity.badRequest().build();
    }
    return ticketReadingUseCase
        .get(organization, ticket, accountId)
        .map(TicketDetailResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  ResponseEntity<TicketListResponse> list(
      @PathVariable String organizationId,
      @RequestParam MultiValueMap<String, String> parameters,
      @AuthenticationPrincipal UUID accountId) {
    UUID organization = uuid(organizationId);
    TicketListQuery query = listQuery(organization, accountId, parameters);
    if (query == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      return ticketReadingUseCase
          .list(query)
          .map(TicketListResponse::from)
          .map(ResponseEntity::ok)
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.badRequest().build();
    }
  }

  private static TicketListQuery listQuery(
      UUID organizationId, UUID accountId, MultiValueMap<String, String> parameters) {
    if (organizationId == null
        || accountId == null
        || !LIST_PARAMETERS.containsAll(parameters.keySet())
        || parameters.values().stream().anyMatch(values -> values.size() != 1)) {
      return null;
    }
    Integer page = integer(parameters, "page", 0);
    Integer size = integer(parameters, "size", 50);
    Ticket.Status status = enumeration(parameters, "status", Ticket.Status.class);
    Ticket.Priority priority = enumeration(parameters, "priority", Ticket.Priority.class);
    UUID customerId = uuid(parameters, "customerId");
    UUID assigneeAccountId = uuid(parameters, "assigneeAccountId");
    if (page == null
        || page < 0
        || size == null
        || size < 1
        || size > 100
        || invalidEnum(parameters, "status", status)
        || invalidEnum(parameters, "priority", priority)
        || invalidUuid(parameters, "customerId", customerId)
        || invalidUuid(parameters, "assigneeAccountId", assigneeAccountId)) {
      return null;
    }
    return new TicketListQuery(
        organizationId, accountId, page, size, status, priority, customerId, assigneeAccountId);
  }

  private static Integer integer(
      MultiValueMap<String, String> parameters, String name, int defaultValue) {
    if (!parameters.containsKey(name)) {
      return defaultValue;
    }
    try {
      return Integer.valueOf(parameters.get(name).getFirst());
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private static <T extends Enum<T>> T enumeration(
      MultiValueMap<String, String> parameters, String name, Class<T> type) {
    if (!parameters.containsKey(name)) {
      return null;
    }
    try {
      return Enum.valueOf(type, parameters.get(name).getFirst());
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private static boolean invalidEnum(
      MultiValueMap<String, String> parameters, String name, Enum<?> value) {
    return parameters.containsKey(name) && value == null;
  }

  private static UUID uuid(MultiValueMap<String, String> parameters, String name) {
    return parameters.containsKey(name) ? uuid(parameters.get(name).getFirst()) : null;
  }

  private static boolean invalidUuid(
      MultiValueMap<String, String> parameters, String name, UUID value) {
    return parameters.containsKey(name) && value == null;
  }

  private static OpenTicketCommand command(
      UUID organizationId, UUID accountId, Map<String, Object> body) {
    if (organizationId == null
        || accountId == null
        || body == null
        || !FIELDS.containsAll(body.keySet())) {
      return null;
    }
    Object title = body.get("title");
    Object description = body.get("description");
    UUID customerId = uuid(body.get("customerId"));
    UUID assigneeAccountId =
        body.containsKey("assigneeAccountId") ? uuid(body.get("assigneeAccountId")) : null;
    Ticket.Priority priority = priority(body.get("priority"));
    if (!(title instanceof String value)
        || (description != null && !(description instanceof String))
        || customerId == null
        || (body.containsKey("priority") && body.get("priority") == null)
        || (body.containsKey("assigneeAccountId") && assigneeAccountId == null)
        || priority == null) {
      return null;
    }
    return new OpenTicketCommand(
        organizationId,
        accountId,
        customerId,
        assigneeAccountId,
        value.trim(),
        (String) description,
        priority);
  }

  private static Ticket.Priority priority(Object value) {
    if (value == null) {
      return Ticket.Priority.MEDIUM;
    }
    if (!(value instanceof String text)) {
      return null;
    }
    try {
      return Ticket.Priority.valueOf(text);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private static UUID uuid(Object value) {
    if (!(value instanceof String text)) {
      return null;
    }
    try {
      return UUID.fromString(text);
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  private record TicketResponse(
      UUID id,
      UUID customerId,
      UUID creatorAccountId,
      UUID assigneeAccountId,
      String title,
      String description,
      Ticket.Priority priority,
      Ticket.Status status,
      Instant createdAt,
      Instant updatedAt) {
    private static TicketResponse from(Ticket ticket) {
      return new TicketResponse(
          ticket.id(),
          ticket.customerId(),
          ticket.creatorAccountId(),
          ticket.assigneeAccountId(),
          ticket.title(),
          ticket.description(),
          ticket.priority(),
          ticket.status(),
          ticket.createdAt(),
          ticket.updatedAt());
    }
  }

  private record TicketDetailResponse(
      UUID id,
      String title,
      String description,
      Ticket.Status status,
      Ticket.Priority priority,
      UUID customerId,
      UUID creatorAccountId,
      UUID assigneeAccountId,
      Instant createdAt,
      Instant updatedAt) {
    private static TicketDetailResponse from(Ticket ticket) {
      return new TicketDetailResponse(
          ticket.id(),
          ticket.title(),
          ticket.description(),
          ticket.status(),
          ticket.priority(),
          ticket.customerId(),
          ticket.creatorAccountId(),
          ticket.assigneeAccountId(),
          ticket.createdAt(),
          ticket.updatedAt());
    }
  }

  private record TicketSummaryResponse(
      UUID id,
      String title,
      Ticket.Status status,
      Ticket.Priority priority,
      UUID customerId,
      UUID assigneeAccountId,
      Instant createdAt,
      Instant updatedAt) {
    private static TicketSummaryResponse from(Ticket ticket) {
      return new TicketSummaryResponse(
          ticket.id(),
          ticket.title(),
          ticket.status(),
          ticket.priority(),
          ticket.customerId(),
          ticket.assigneeAccountId(),
          ticket.createdAt(),
          ticket.updatedAt());
    }
  }

  private record TicketListResponse(
      List<TicketSummaryResponse> items, int page, int size, long totalElements, long totalPages) {
    private static TicketListResponse from(TicketPage page) {
      return new TicketListResponse(
          page.items().stream().map(TicketSummaryResponse::from).toList(),
          page.page(),
          page.size(),
          page.totalElements(),
          page.totalPages());
    }
  }
}
