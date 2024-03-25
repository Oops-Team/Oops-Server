package com.oops.server.controller;

import com.oops.server.dto.request.PwdCodeVerificationRequest;
import com.oops.server.security.TokenProvider;
import com.oops.server.service.FindIdAndPwdService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ID/PW 찾기 관련
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FindIdAndPwdController {

    private final TokenProvider tokenProvider;
    private final FindIdAndPwdService findIdAndPwdService;

    // 이메일 찾기
    @GetMapping("/find/email/{email}")
    public ResponseEntity findEmail(@PathVariable("email") String email) {
        return findIdAndPwdService.findEmail(email);
    }

    // 비밀번호 찾기 - 인증 코드 전송
    @GetMapping("/find/password/{email}")
    public ResponseEntity sendCode(@PathVariable("email") String email) {
        return findIdAndPwdService.sendCode(email);
    }

    // 비밀번호 찾기 - 코드 인증
    @PostMapping("/find/password")
    public ResponseEntity verificationCode(@RequestBody PwdCodeVerificationRequest request) {
        return findIdAndPwdService.verificationCode(request);
    }

    // 새로운 비밀번호로 변경
    @PatchMapping("/change/password")
    public ResponseEntity changePassword(@RequestHeader("tempToken") String token,
            @RequestBody Map<String, String> passwordMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return findIdAndPwdService.modifyPassword(userId, passwordMap.get("password"));
    }
}
