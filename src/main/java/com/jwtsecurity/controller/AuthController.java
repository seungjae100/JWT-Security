package com.jwtsecurity.controller;

import com.jwtsecurity.dto.request.LoginRequest;
import com.jwtsecurity.dto.request.RegisterRequest;
import com.jwtsecurity.dto.response.AuthResponse;
import com.jwtsecurity.dto.response.ErrorResponse;
import com.jwtsecurity.dto.response.SuccessResponse;
import com.jwtsecurity.jwt.JwtService;
import com.jwtsecurity.service.AuthService;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        try {
            AuthResponse authResponse = authService.register(request);
            // 쿠키에 사용자 정보 저장
            addUserInfoCookie(response, authResponse);
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
            // 쿠키게 사용자 정보 저장
            addUserInfoCookie(response, authResponse);
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
                clearCookie(request, response);

                return ResponseEntity.ok(new SuccessResponse("로그아웃이 완료되었습니다."));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorResponse("로그아웃 처리 중 오류가 발생했습니다."));
            }
        }
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("유효하지 않은 토큰입니다."));
    }

    private void addUserInfoCookie(HttpServletResponse response, AuthResponse authResponse) {
        Cookie usernameCookie = new Cookie("userName", authResponse.getName());
        Cookie emailCookie = new Cookie("userEmail", authResponse.getEmail());

        // 쿠키 설정
        usernameCookie.setHttpOnly(true);
        emailCookie.setHttpOnly(true);
        // 로컬 개발 환경에서는 secure = false 로 설정 (HTTP에서도 동작하도록)
        // 로컬 개발 환경에서는 대개 HTTP를 사용하므로 쿠키가 전송되지 않음
        // secure=true 는 HTTPS에서만 쿠키 전송을 허용
        // 실제 운영 환경에서는 다시 true로 설정해야함
        usernameCookie.setSecure(false); // 실제 개발에서는 true
        emailCookie.setSecure(false); // 실제 개발에서는 true
        usernameCookie.setPath("/");
        emailCookie.setPath("/");
        usernameCookie.setDomain("localhost");  // 추가
        emailCookie.setDomain("localhost");     // 추가

        // 쿠키 만료 시간 설정
        usernameCookie.setMaxAge(1800);
        emailCookie.setMaxAge(1800);

        response.addCookie(usernameCookie);
        response.addCookie(emailCookie);


    }

    // 쿠키 삭제 메서드
    private void clearCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
