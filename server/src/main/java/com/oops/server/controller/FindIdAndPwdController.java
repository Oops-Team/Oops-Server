package com.oops.server.controller;

import com.oops.server.service.FindIdAndPwdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/find")
@RequiredArgsConstructor
public class FindIdAndPwdController {


    private final FindIdAndPwdService findIdAndPwdService;


    // ID/PW 찾기 - 이메일 찾기
    @GetMapping("/find/email/{email}")
    public ResponseEntity findEmail(@PathVariable("email") String email) {
        return findIdAndPwdService.findEmail(email);
    }

    // ID/PW 찾기 - 비밀번호 찾기 - 인증 코드 전송
    @GetMapping("/find/password/{email}")
    public ResponseEntity sendCode(@PathVariable("email") String email) {
        return findIdAndPwdService.sendCode(email);
    }
}
