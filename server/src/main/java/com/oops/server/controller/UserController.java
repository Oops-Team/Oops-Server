package com.oops.server.controller;

import com.oops.server.context.ExceptionMessages;
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
    public ResponseEntity checkNickname(@PathVariable("name") String name) {
        boolean isValidate = userService.validateDuplicateName(name);

        if (isValidate) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.OK, "성공"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity(DefaultResponse.from(StatusCode.CONFLICT,
                    ExceptionMessages.DUPLICATE_NAME.get()),
                    HttpStatus.CONFLICT);
        }
    }

    // Oops 회원 이메일 중복 검사
    @GetMapping("/email/{email}")
    public ResponseEntity checkOopsEmail(@PathVariable("email") String email) {
        boolean isValidate = userService.validateDuplicateUser(email, "oops");

        if (isValidate) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.OK, "성공"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity(DefaultResponse.from(StatusCode.CONFLICT,
                    ExceptionMessages.DUPLICATE_USER.get()),
                    HttpStatus.CONFLICT);
        }
    }

    // Oops 회원가입
    @PostMapping("/sign-up")
    public ResponseEntity createOopsUser(@RequestBody SignUpRequest request) {
        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", userService.join(request)),
                HttpStatus.OK);
    }

    // 로그인
    @GetMapping("/login/{loginId}")
    public ResponseEntity login(@PathVariable("loginId") String loginType,
            @RequestBody SignUpRequest request) {

        switch (loginType) {
            // oops 로그인
            case "oops":
                return userService.signInOops(request);

            // 소셜 로그인
            case "naver":
            case "google":
                return userService.signInSocial(request, loginType);

            default:
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.BAD_REQUEST,
                                ExceptionMessages.BAD_REQUEST.get()),
                        HttpStatus.BAD_REQUEST);
        }
    }
}
