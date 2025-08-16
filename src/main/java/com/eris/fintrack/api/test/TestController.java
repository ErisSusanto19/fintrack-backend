package com.eris.fintrack.api.test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/greetings")
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        return ResponseEntity.ok("Hello, " + currentPrincipalName + "! You have accessed a protected resource.");
    }
}