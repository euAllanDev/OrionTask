package com.oriontask.organization.adapter.out.security;

import com.oriontask.organization.application.port.out.InvitationTokenDeriver;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HmacInvitationTokenDeriver implements InvitationTokenDeriver {
  private final byte[] key;

  HmacInvitationTokenDeriver(
      @Value("${oriontask.security.invitation-hmac-key:}") String configuredKey) {
    if (configuredKey.isBlank()) {
      throw new IllegalStateException("ORIONTASK_INVITATION_HMAC_KEY must be configured");
    }
    key = Base64.getDecoder().decode(configuredKey);
    if (key.length < 32) {
      throw new IllegalStateException(
          "ORIONTASK_INVITATION_HMAC_KEY must contain at least 256 bits");
    }
  }

  @Override
  public String derive(String token) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(
              mac.doFinal(("membership-invitation:" + token).getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Unable to derive invitation token", exception);
    }
  }
}
