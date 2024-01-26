package com.oops.server.controller;

import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/sign-up")
    public ResponseEntity createOopsUser(@RequestBody SignUpRequest request) {
        return new ResponseEntity(DefaultResponse.from(StatusCode.OK, "성공", userService.join(request)),
                HttpStatus.OK);
    }
}
