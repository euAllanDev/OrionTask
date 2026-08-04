package com.oriontask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class OrionTaskApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrionTaskApplication.class, args);
  }
}
