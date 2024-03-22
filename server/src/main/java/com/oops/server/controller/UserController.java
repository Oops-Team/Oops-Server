package com.oops.server.controller;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.AccountDeleteRequest;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.security.TokenProvider;
import com.oops.server.service.FindIdAndPwdService;
import com.oops.server.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FindIdAndPwdService findIdAndPwdService;
    private final TokenProvider tokenProvider;

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
    @PostMapping("/login/{loginId}")
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

    // 탙퇴
    @DeleteMapping("")
    public ResponseEntity deleteAccount(@RequestHeader("xAuthToken") String token,
                                        @RequestBody AccountDeleteRequest request) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return userService.deleteAccount(userId, request);
    }

    // ID/PW 찾기 - 이메일 찾기
    @GetMapping("/find/email/{email}")
    public ResponseEntity findEmail(@PathVariable("email") String email) {
        return findIdAndPwdService.findEmail(email);
    }

    // 마이페이지 조회
    @GetMapping("/mypage")
    public ResponseEntity getMyPage(@RequestHeader("xAuthToken") String token) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return userService.getMyPage(userId);
    }

    // 프로필 공개 설정 변경
    @PatchMapping("/mypage/profile")
    public ResponseEntity modifyPublic(@RequestHeader("xAuthToken") String token,
            @RequestBody Map<String, Boolean> isPublicMap) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        return userService.modifyPublic(userId, isPublicMap.get("isPublic"));
    }

    // 공지사항 조회
    @GetMapping("/notices")
    public ResponseEntity getAllNotices() {
        return userService.getAllNotices();
    }
}
