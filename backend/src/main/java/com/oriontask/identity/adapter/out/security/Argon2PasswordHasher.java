package com.oriontask.identity.adapter.out.security;

import com.oriontask.identity.application.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class Argon2PasswordHasher implements PasswordHasher {
  private final Argon2PasswordEncoder encoder =
      Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  private final String preparedHash = encoder.encode("oriontask-login-dummy-password");

  @Override
  public String hash(String password) {
    return encoder.encode(password);
  }

  @Override
  public boolean matches(String password, String passwordHash) {
    return encoder.matches(password, passwordHash);
  }

  @Override
  public boolean matchesPreparedHash(String password) {
    return encoder.matches(password, preparedHash);
  }
}
