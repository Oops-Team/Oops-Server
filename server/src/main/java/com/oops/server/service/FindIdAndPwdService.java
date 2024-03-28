package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.PwdCodeVerificationRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import com.oops.server.security.TokenProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindIdAndPwdService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder encoder;

    // 인증번호 자리수
    private final int CODE_LENGTH = 5;

    // 인증번호 생성 메소드 (랜덤 난수 5자리)
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int ranNum = random.nextInt(10);    // 랜덤 숫자 생성
            code.append(ranNum);    // 덧붙이기
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
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_EMAIL.get()),
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
        String subject = "[Oops] 인증코드 발송";
        String content = "<h3 style=\"text-align: center;\"> 인증코드 </h3>";
        content += "<h1 style=\"text-align: center;\">" + code + "</h1>";
        content += "<br> <br> <p style=\"text-align: center;\"> 비밀번호 찾기 인증코드가 발급되었습니다. </p>";
        content += "<br> <p style=\"text-align: center;\"> 위 숫자를 인증코드 입력 칸에 기입해주세요! </p>";

        // 이메일 보내기
        emailService.send(email, subject, content);

        // 인증번호 저장
        redisService.saveVerificationCode(email, code);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증코드 검증
    public ResponseEntity verificationCode(PwdCodeVerificationRequest request) {
        // 해당 이메일의 코드 가져오기
        String verificationCode = redisService.getVerificationCode(request.email());
        log.debug("가져온 인증 코드 : " + verificationCode);

        // 만일 인증 코드가 만료되었다면
        if (verificationCode == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.UNAUTHORIZED,
                            ExceptionMessages.EXPIRATION_VERIFICATION_CODE.get()),
                    HttpStatus.UNAUTHORIZED);
        }

        // 사용자가 입력한 인증 코드 검증
        // 올바른 인증 코드를 입력했다면
        if (verificationCode.equals(request.code())) {
            Long userId = null;

            try {
                // 해당 사용자의 아이디 값 가져오기
                User user = userRepository.findByEmailAndSnsType(request.email(), "oops");
                userId = user.getUserId();
            } catch (NullPointerException e) {
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.NOT_FOUND,
                                ExceptionMessages.NOT_FOUND_USER.get()),
                        HttpStatus.NOT_FOUND);
            }

            // 임시 토큰 발급
            String tempToken = tokenProvider.createTempToken(userId);
            
            // 응답 dto 구성
            Map<String, String> tempTokenMap = new HashMap<>();
            tempTokenMap.put("tempToken", tempToken);

            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, "성공", tempTokenMap),
                    HttpStatus.OK);
        }
        // 올바르지 않은 인증 코드를 입력했다면
        else {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.BAD_REQUEST,
                            ExceptionMessages.MISS_MATCH_VERIFICATION_CODE.get()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    // 새로운 비밀번호로 변경
    public ResponseEntity modifyPassword(Long userId, String password) {
        // 해당 유저 정보 가져오기
        User user = userRepository.findByUserId(userId);

        // 기존 비밀번호와 동일하다면
        if (encoder.matches(password, user.getPassword())) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.CONFLICT,
                            ExceptionMessages.CONFLICT_OLD_PASSWORD.get()),
                    HttpStatus.CONFLICT);
        }

        // 비밀번호 변경 및 적용
        user.modifyPassword(password, encoder);
        userRepository.save(user);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
