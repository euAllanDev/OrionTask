package com.oriontask.identity.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TrustedSourceIpResolver {
  private final Set<String> trustedProxies;

  TrustedSourceIpResolver(
      @Value("${oriontask.security.trusted-proxies:}") String configuredProxies) {
    trustedProxies =
        Arrays.stream(configuredProxies.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
  }

  String resolve(HttpServletRequest request) {
    String remoteAddress = request.getRemoteAddr();
    if (!trustedProxies.contains(remoteAddress)) {
      return remoteAddress;
    }
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor == null || forwardedFor.isBlank()) {
      return remoteAddress;
    }
    return forwardedFor.split(",", 2)[0].trim();
  }
}
