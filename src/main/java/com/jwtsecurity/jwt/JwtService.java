package com.jwtsecurity.jwt;

import com.jwtsecurity.domain.RefreshToken;
import com.jwtsecurity.repository.RefreshTokenRepository;
import com.jwtsecurity.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;

    // Access Token 생성
    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getAccessTokenExpiration());
    }
    // Refresh Token 생성
    @Transactional
    public String generateRefreshToken(UserDetails userDetails) {
        String refreshToken = buildToken(
                new HashMap<>(),
                userDetails,
                jwtProperties.getRefreshTokenExpiration()
        );

        saveRefreshToken(userDetails.getUsername(), refreshToken);

        return refreshToken;
    }

    // Refresh Token 저장 또는 갱신
    private void saveRefreshToken(String username, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUsername(username)
                .orElse(new RefreshToken());

        token.setUsername(username);
        token.setToken(refreshToken);
        token.setExpiryDate(LocalDateTime.now()
                .plusNanos(jwtProperties.getRefreshTokenExpiration() * 1_000_000));

        refreshTokenRepository.save(token);
    }

    // 토큰에서 사용자 이름 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 토큰 유효성 확인
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Refresh Token 유효성 확인
    public boolean isRefreshTokenValid(String refreshToken, String username) {
        Optional<RefreshToken> storedToken = refreshTokenRepository.findByUsername(username);
        return storedToken.filter(token -> token.getToken().equals(refreshToken)
                && token.getExpiryDate().isAfter(LocalDateTime.now())).isPresent();
    }
    // 새로운 AccessToken 발급
    @Transactional
    public String reissueAccessToken(String username) {
        UserDetails userDetails = refreshTokenRepository.findByUsername(username)
                .map(RefreshToken::getUsername)
                .map(customUserDetailsService::loadUserByUsername)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        return generateToken(userDetails);
    }

    // 내부 유틸리티 메서드
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInkey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key getSignInkey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
