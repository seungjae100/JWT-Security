package com.jwtsecurity.jwt;

import com.jwtsecurity.domain.RefreshToken;
import com.jwtsecurity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String refreshToken = buildToken(
                new HashMap<>(),
                userDetails,
                jwtProperties.getRefreshTokenExpiration()
        );

        saveRefreshToken(userDetails.getUsername(), refreshToken);

        return refreshToken;
    }

    private void saveRefreshToken(String username, String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByUsername(username)
                .orElse(new RefreshToken());

        token.setUsername(username);
        token.setToken(refreshToken);
        token.setExpiryDate(LocalDateTime.now()
                .plusNanos(jwtProperties.getRefreshTokenExpiration() * 1_000_000));

        refreshTokenRepository.save(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

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

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
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
