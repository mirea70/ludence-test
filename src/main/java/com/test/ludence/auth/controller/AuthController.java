package com.test.ludence.auth.controller;

import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.auth.dto.response.TokenResponse;
import com.test.ludence.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new TokenResponse(authService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(new TokenResponse(authService.login(request)));
    }
}
