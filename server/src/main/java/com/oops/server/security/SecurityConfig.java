package com.oops.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private TokenProvider tokenProvider;
    private final String[] allowedURIs = {"/", "/user/nickname/**", "/user/email/**",
            "/user/find/email/**", "/user/sign-up", "/user/login/**"};

    // 특정 URI는 security filter를 거치지 않도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                         .requestMatchers(PathRequest.toH2Console())    // h2 console 관련 uri
                         .requestMatchers(PathRequest.toStaticResources().atCommonLocations())  // 정적 리소스 uri
                         .requestMatchers(allowedURIs);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)  // 쿠키 사용 안 함
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers(allowedURIs)
                                .permitAll()    // requestMatchers의 인자로 전달된 url은 모두에게 허용
                                .anyRequest().authenticated()    // 그 외의 모든 요청은 인증 필요
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )    // 세션을 사용하지 않으므로 STATELESS 설정
                .addFilterBefore(new JwtAuthenticateFilter(tokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
