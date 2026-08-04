package com.oriontask.identity.application.port.out;

public interface PasswordHasher {
  String hash(String password);

  default boolean matches(String password, String passwordHash) {
    throw new UnsupportedOperationException("Password matching is not configured");
  }

  default boolean matchesPreparedHash(String password) {
    throw new UnsupportedOperationException("Password matching is not configured");
  }
}
