package com.oriontask.identity.adapter.out.security;

import com.oriontask.identity.application.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class Argon2PasswordHasher implements PasswordHasher {
  private final Argon2PasswordEncoder encoder =
      Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

  @Override
  public String hash(String password) {
    return encoder.encode(password);
  }
}
