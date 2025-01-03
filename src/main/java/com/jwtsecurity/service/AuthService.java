package com.jwtsecurity.service;

import com.jwtsecurity.domain.User;
import com.jwtsecurity.dto.request.LoginRequest;
import com.jwtsecurity.dto.request.RegisterRequest;
import com.jwtsecurity.dto.response.AuthResponse;
import com.jwtsecurity.jwt.JwtService;
import com.jwtsecurity.repository.RefreshTokenRepository;
import com.jwtsecurity.repository.UserRepository;
import com.jwtsecurity.security.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        // AccessToken 생성
        String accessToken = jwtService.generateToken(userDetails);
        // Refresh Token 생성 및 저장
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // AccessToken 생성
        String accessToken = jwtService.generateToken(userDetails);

        // Refresh Token 생성 및 저장
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
    @Transactional
    public void logout(String username) {
        try {
            // deleteByUsername을 호출하여 직접 삭제
            refreshTokenRepository.deleteByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
}
