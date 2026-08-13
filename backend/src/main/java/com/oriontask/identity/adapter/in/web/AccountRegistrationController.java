package com.oriontask.identity.adapter.in.web;

import com.oriontask.identity.application.port.in.RegisterAccountUseCase;
import com.oriontask.identity.application.port.in.RegistrationCommand;
import com.oriontask.identity.application.port.in.RegistrationResult;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
class AccountRegistrationController {
  private final RegisterAccountUseCase registerAccountUseCase;

  AccountRegistrationController(RegisterAccountUseCase registerAccountUseCase) {
    this.registerAccountUseCase = registerAccountUseCase;
  }

  @PostMapping
  ResponseEntity<Void> register(@RequestBody Map<String, Object> body, HttpServletRequest request) {
    if (!body.keySet().equals(java.util.Set.of("email", "password"))
        || !(body.get("email") instanceof String email)
        || !(body.get("password") instanceof String password)) {
      return ResponseEntity.badRequest().build();
    }

    RegistrationResult result =
        registerAccountUseCase.register(
            new RegistrationCommand(email, password, request.getRemoteAddr()));
    if (result == RegistrationResult.INVALID) {
      return ResponseEntity.badRequest().build();
    }
    if (result == RegistrationResult.RATE_LIMITED) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
    return ResponseEntity.accepted().build();
  }
}
