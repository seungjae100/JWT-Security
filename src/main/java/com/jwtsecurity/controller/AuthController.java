package com.jwtsecurity.controller;

import com.jwtsecurity.dto.request.LoginRequest;
import com.jwtsecurity.dto.request.RegisterRequest;
import com.jwtsecurity.dto.response.AuthResponse;
import com.jwtsecurity.dto.response.ErrorResponse;
import com.jwtsecurity.dto.response.SuccessResponse;
import com.jwtsecurity.jwt.JwtService;
import com.jwtsecurity.security.user.CustomUserDetails;
import com.jwtsecurity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            String username = jwtService.extractUsername(accessToken);

            // RefreshToken을 DB에서 삭제
            authService.logout(username);

            return ResponseEntity.ok()
                    .body(new SuccessResponse("로그아웃이 되었습니다."));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("유효하지 않은 토큰입니다."));
    }
}
