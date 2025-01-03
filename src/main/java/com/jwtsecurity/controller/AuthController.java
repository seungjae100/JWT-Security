package com.jwtsecurity.controller;

import com.jwtsecurity.dto.request.LoginRequest;
import com.jwtsecurity.dto.request.RegisterRequest;
import com.jwtsecurity.dto.response.AuthResponse;
import com.jwtsecurity.dto.response.ErrorResponse;
import com.jwtsecurity.dto.response.SuccessResponse;
import com.jwtsecurity.jwt.JwtService;
import com.jwtsecurity.service.AuthService;
import com.jwtsecurity.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.register(request);
            cookieUtil.addUserInfoCookie(response, authResponse);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.login(request);
            cookieUtil.addUserInfoCookie(response, authResponse);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String accessToken = authHeader.substring(7);
                String username = jwtService.extractUsername(accessToken);

                // RefreshToken을 DB에서 삭제
                authService.logout(username);

                // 쿠키 삭제
                cookieUtil.clearCookies(request, response);

                return ResponseEntity.ok(new SuccessResponse("로그아웃이 완료되었습니다."));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("로그아웃 처리 중 오류가 발생했습니다."));
            }
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("유효하지 않은 토큰입니다."));
    }


}
