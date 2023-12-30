package com.oops.server.controller;

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
    public void checkNickname(@PathVariable String name) {
        userService.validateDuplicateName(name);
    }

    // Oops 회원 이메일 중복 검사
    @GetMapping("/email/{email}")
    public void checkOopsEmail(@PathVariable String email) {
        userService.validateDuplicateUser(email, "oops");
    }
}
