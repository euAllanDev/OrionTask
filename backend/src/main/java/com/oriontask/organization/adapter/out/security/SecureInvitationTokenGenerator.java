package com.oriontask.organization.adapter.out.security;

import com.oriontask.organization.application.port.out.InvitationTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
class SecureInvitationTokenGenerator implements InvitationTokenGenerator {
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public String generate() {
    byte[] value = new byte[32];
    secureRandom.nextBytes(value);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
  }
}
