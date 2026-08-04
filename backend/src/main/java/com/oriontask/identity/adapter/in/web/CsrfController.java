package com.oriontask.identity.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/csrf")
class CsrfController {
  @GetMapping
  ResponseEntity<Map<String, String>> csrf(HttpServletRequest request) {
    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    return ResponseEntity.ok(Map.of("token", token.getToken()));
  }
}
