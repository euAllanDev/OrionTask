package com.oriontask.organization.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class OrganizationMembershipMigrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

  @Test
  void preservesOwnerAndAcceptsOnlyApprovedRoles() throws SQLException {
    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .target(MigrationVersion.fromVersion("3"))
        .load()
        .migrate();
    UUID accountId = UUID.randomUUID();
    UUID organizationId = UUID.randomUUID();

    try (Connection connection = connection()) {
      insertAccount(connection, accountId);
      insertOrganization(connection, organizationId);
      insertMembership(connection, organizationId, accountId, "OWNER");
    }

    Flyway.configure()
        .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
        .load()
        .migrate();

    try (Connection connection = connection()) {
      assertThat(roleOf(connection, organizationId, accountId)).isEqualTo("OWNER");
      insertMembership(connection, organizationId, UUID.randomUUID(), "ADMIN");
      insertMembership(connection, organizationId, UUID.randomUUID(), "TECHNICIAN");
      assertThatThrownBy(
              () -> insertMembership(connection, organizationId, UUID.randomUUID(), "INVALID"))
          .isInstanceOf(SQLException.class);
    }
  }

  private Connection connection() throws SQLException {
    return DriverManager.getConnection(
        POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  }

  private static void insertAccount(Connection connection, UUID accountId) throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into identity_accounts (id, normalized_email, password_hash, created_at) "
                + "values (?, ?, ?, current_timestamp)")) {
      statement.setObject(1, accountId);
      statement.setString(2, accountId + "@example.com");
      statement.setString(3, "test-hash");
      statement.executeUpdate();
    }
  }

  private static void insertOrganization(Connection connection, UUID organizationId)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into organizations (id, name, created_at, updated_at) "
                + "values (?, ?, current_timestamp, current_timestamp)")) {
      statement.setObject(1, organizationId);
      statement.setString(2, "Migration Organization");
      statement.executeUpdate();
    }
  }

  private static void insertMembership(
      Connection connection, UUID organizationId, UUID accountId, String role) throws SQLException {
    insertAccountIfMissing(connection, accountId);
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into organization_memberships "
                + "(id, organization_id, account_id, role, created_at, updated_at) "
                + "values (?, ?, ?, ?, current_timestamp, current_timestamp)")) {
      statement.setObject(1, UUID.randomUUID());
      statement.setObject(2, organizationId);
      statement.setObject(3, accountId);
      statement.setString(4, role);
      statement.executeUpdate();
    }
  }

  private static void insertAccountIfMissing(Connection connection, UUID accountId)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "insert into identity_accounts (id, normalized_email, password_hash, created_at) "
                + "values (?, ?, ?, current_timestamp) on conflict (id) do nothing")) {
      statement.setObject(1, accountId);
      statement.setString(2, accountId + "@example.com");
      statement.setString(3, "test-hash");
      statement.executeUpdate();
    }
  }

  private static String roleOf(Connection connection, UUID organizationId, UUID accountId)
      throws SQLException {
    try (PreparedStatement statement =
        connection.prepareStatement(
            "select role from organization_memberships where organization_id = ? and account_id = ?")) {
      statement.setObject(1, organizationId);
      statement.setObject(2, accountId);
      try (ResultSet result = statement.executeQuery()) {
        result.next();
        return result.getString("role");
      }
    }
  }
}
