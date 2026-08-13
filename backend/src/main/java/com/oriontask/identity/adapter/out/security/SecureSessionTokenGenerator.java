package com.oriontask.identity.adapter.out.security;

import com.oriontask.identity.application.port.out.SessionTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
class SecureSessionTokenGenerator implements SessionTokenGenerator {
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generate() {
    byte[] value = new byte[32];
    secureRandom.nextBytes(value);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }
}
