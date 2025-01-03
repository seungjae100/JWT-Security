package com.jwtsecurity.util;

import com.jwtsecurity.dto.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component // 스프링 컴포넌트로 등록하여 의존서 주입이 가능하게 함
public class CookieUtil {
    // 사용자 정보 (이메일, 이름) 를 쿠키에 저장하는 메서드
    public void addUserInfoCookie(HttpServletResponse response, AuthResponse authResponse) {
        addCookie(response, "userName", authResponse.getName(), 1800);
        addCookie(response, "userEmail", authResponse.getEmail(), 1800);
    }

    // 모든 쿠키를 삭제하는 메서드
    public void clearCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies(); // 현재 요청에 포함된 모든 쿠키 가져오기
        if (cookies != null) {
            for (Cookie cookie : cookies) {      // 각 쿠키에 대해
                removeCookie(response, cookie.getName()); // 쿠키 삭제
            }
        }
    }

    // 새로운 쿠키를 생성하고 설정하는 private 헬퍼 메서드
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value); // 새 쿠키 생성
        cookie.setHttpOnly(true);                // JavaScript에서 접근이 불가능하게 설정
        cookie.setSecure(false);                 // 개발 환경 htttp에서도 전송가능하게 설정
        cookie.setPath("/");                     // 모든 경로에서 접근 가능하게 설정
        cookie.setDomain("localhost");           // 로컬 도메일 설정
        cookie.setMaxAge(maxAge);                // 쿠키 만료시간 설정 ( 초단위 )
        response.addCookie(cookie);              // 응답에 쿠키 추가
    }

    // 쿠키를 삭제하는 private 헬퍼 메서드
    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");    // 빈 값을 가진 새 쿠키 설정
        cookie.setPath("/");                           // 모든 경로에서 접근 가능하게 설정
        cookie.setMaxAge(0);                           // 즉시 만료되도록 설정
        response.addCookie(cookie);                    // 응답에 쿠키 추가
    }


}
