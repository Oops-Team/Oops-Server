package com.oops.server.controller;

import com.oops.server.dto.user.SignUpRequest;
import com.oops.server.dto.user.SignUpResponse;
import com.oops.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // 닉네임 중복 검사
    @GetMapping("/nickname/{name}")
    public void checkNickname(@PathVariable("name") String name) {
        userService.validateDuplicateName(name);
    }

    // Oops 회원 이메일 중복 검사
    @GetMapping("/email/{email}")
    public void checkOopsEmail(@PathVariable("email") String email) {
        userService.validateDuplicateUser(email, "oops");
    }

    // Oops 회원가입
    @PostMapping("/signup")
    public void createOopsUser(@RequestBody SignUpRequest request) {
        userService.join(request);

        // TODO: 토큰 만들기(클래스 따로 생성하는 게 좋을듯)
        // TODO: 응답 데이터 보내기
    }
}
