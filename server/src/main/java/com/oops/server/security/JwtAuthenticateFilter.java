package com.oops.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticateFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 헤더에서 토큰 받아오기
        String token = tokenProvider.resolveToken(request);

        // 토큰 유효 검사
        boolean isGood = false;
        try {
            isGood = !tokenProvider.isTokenExpired(token);
        } catch (IllegalArgumentException e) {
            log.info("요청 uri : " + request.getRequestURI());
            log.error("토큰이 존재하지 않습니다");
        } catch (io.jsonwebtoken.SignatureException e) {
            log.error("토큰의 서명이 올바르지 않습니다");
        }

        // 토큰이 유효하다면
        if (token != null && isGood) {
            // 토큰으로부터 유저 정보를 받아
            Authentication authentication = tokenProvider.getAuthentication(token);
            // 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 다음 Filter 실행
        filterChain.doFilter(request, response);
    }
}
