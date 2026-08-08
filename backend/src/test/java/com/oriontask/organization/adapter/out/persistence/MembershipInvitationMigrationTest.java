package com.oriontask.organization.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MembershipInvitationMigrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @BeforeAll
  static void migrate() {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .load()
        .migrate();
  }

  @Test
  void allowsReissueAfterExpiryButRejectsTwoPendingInvitations() throws SQLException {
    UUID accountId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    try (Connection connection = connection()) {
      connection
          .createStatement()
          .execute(
              "insert into identity_accounts (id, normalized_email, password_hash, created_at) values ('"
                  + accountId
                  + "', 'recipient@example.com', 'hash', current_timestamp)");
      connection
          .createStatement()
          .execute(
              "insert into organizations (id, name, created_at, updated_at) values ('"
                  + organizationId
                  + "', 'Organization', current_timestamp, current_timestamp)");
      insertInvitation(connection, organizationId, accountId, "PENDING");
      assertThatThrownBy(() -> insertInvitation(connection, organizationId, accountId, "PENDING"))
          .isInstanceOf(SQLException.class);
      connection
          .createStatement()
          .execute(
              "update membership_invitations set status = 'EXPIRED' where organization_id = '"
                  + organizationId
                  + "'");
      insertInvitation(connection, organizationId, accountId, "PENDING");
    }
  }

  @Test
  void requiresAcceptedTimestampOnlyForAcceptedInvitation() throws SQLException {
    UUID accountId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();
    try (Connection connection = connection()) {
      connection
          .createStatement()
          .execute(
              "insert into identity_accounts (id, normalized_email, password_hash, created_at) values ('"
                  + accountId
                  + "', 'timestamp@example.com', 'hash', current_timestamp)");
      connection
          .createStatement()
          .execute(
              "insert into organizations (id, name, created_at, updated_at) values ('"
                  + organizationId
                  + "', 'Organization', current_timestamp, current_timestamp)");

      assertThatThrownBy(
              () ->
                  connection
                      .createStatement()
                      .execute(
                          "insert into membership_invitations (id, organization_id, recipient_account_id, role, "
                              + "token_derivation, status, expires_at, created_at) values ('"
                              + UUID.randomUUID()
                              + "', '"
                              + organizationId
                              + "', '"
                              + accountId
                              + "', 'TECHNICIAN', '"
                              + UUID.randomUUID()
                              + "', 'ACCEPTED', current_timestamp + interval '7 days', current_timestamp)"))
          .isInstanceOf(SQLException.class);
    }
  }

  private Connection connection() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void insertInvitation(
      Connection connection, UUID organizationId, UUID accountId, String status)
      throws SQLException {
    connection
        .createStatement()
        .execute(
            "insert into membership_invitations (id, organization_id, recipient_account_id, role, "
                + "token_derivation, status, expires_at, created_at) values ('"
                + UUID.randomUUID()
                + "', '"
                + organizationId
                + "', '"
                + accountId
                + "', 'TECHNICIAN', '"
                + UUID.randomUUID()
                + "', '"
                + status
                + "', current_timestamp + interval '7 days', current_timestamp)");
  }
}
