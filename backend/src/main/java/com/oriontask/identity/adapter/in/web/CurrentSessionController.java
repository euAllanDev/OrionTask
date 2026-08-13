package com.oriontask.identity.adapter.in.web;

import com.oriontask.identity.application.port.in.CurrentSessionIdentity;
import com.oriontask.identity.application.port.in.GetCurrentSessionIdentityUseCase;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/session")
class CurrentSessionController {
  private final GetCurrentSessionIdentityUseCase getCurrentSessionIdentityUseCase;

  CurrentSessionController(GetCurrentSessionIdentityUseCase getCurrentSessionIdentityUseCase) {
    this.getCurrentSessionIdentityUseCase = getCurrentSessionIdentityUseCase;
  }

  @GetMapping
  ResponseEntity<CurrentSessionIdentity> current(@AuthenticationPrincipal UUID accountId) {
    return getCurrentSessionIdentityUseCase
        .get(accountId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }
}
