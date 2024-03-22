package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindIdAndPwdService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;

    // 인증번호 자리수
    private final int CODE_LENGTH = 5;

    // 인증번호 생성 메소드 (랜덤 난수 5자리)
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int ranNum = random.nextInt(10);
            log.info("랜덤 값 : " + ranNum);

            code.append(ranNum);
            log.info("현재 코드 : " + code);
        }

        return code.toString();
    }

    // 이메일 찾기
    public ResponseEntity findEmail(String email) {
        // 해당 이메일로 가입한 모든 회원 불러오기
        List<User> userList = userRepository.findAllByEmail(email);

        // 만일 해당 이메일로 가입한 계정이 없다면
        if (userList.size() == 0) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ExceptionMessages.NOT_FOUND_EMAIL.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 계정이 하나만 나왔을 경우
        else if (userList.size() == 1) {
            // 그 계정이 소셜 연동 계정일 경우
            if (!userList.get(0).getSnsType().equals("oops")) {
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.OK, "성공", userList.get(0).getSnsType()),
                        HttpStatus.OK);
            }
        }

        // 계정이 2개 나왔거나, 1개인데 oops 계정일 경우
        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증코드 보내기
    public ResponseEntity sendCode(String email) {
        // 정보 구성
        String code = generateCode();
        String subject = "[Oops] 인증 코드 발송";
        String content = "인증 코드 : " + code;

        // 이메일 보내기
        emailService.send(email, subject, content);

        // 인증번호 저장
        redisService.saveVerificationCode(email, code);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증코드 검증
    public ResponseEntity verificationCode(String email, String code) {
        return null;
    }
}
