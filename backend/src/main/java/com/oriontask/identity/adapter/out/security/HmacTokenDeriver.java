package com.oriontask.identity.adapter.out.security;

import com.oriontask.identity.application.port.out.TokenDeriver;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class HmacTokenDeriver implements TokenDeriver {
  private final byte[] key;

  HmacTokenDeriver(@Value("${oriontask.security.session-hmac-key:}") String configuredKey) {
    if (configuredKey.isBlank()) {
      throw new IllegalStateException("ORIONTASK_SESSION_HMAC_KEY must be configured");
    }
    key = Base64.getDecoder().decode(configuredKey);
    if (key.length < 32) {
      throw new IllegalStateException("ORIONTASK_SESSION_HMAC_KEY must contain at least 256 bits");
    }
  }

  @Override
  public String derive(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return Base64.getUrlEncoder()
          .withoutPadding()
          .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Unable to derive protected identifier", exception);
    }
  }
}
