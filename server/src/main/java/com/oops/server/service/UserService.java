package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.request.SignInRequest;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.SignInResponse;
import com.oops.server.entity.User;
//import com.oops.server.entity.UserRefreshToken;
//import com.oops.server.repository.UserRefreshTokenRepository;
import com.oops.server.repository.UserRepository;
import com.oops.server.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    //    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;

    // 중복 회원 검증
    public boolean validateDuplicateUser(String email, String snsType) {
        // 중복시 false 반환
        return userRepository.findByEmailAndSnsType(email, snsType) == null;
    }

    // 중복 닉네임 검증
    public boolean validateDuplicateName(String name) {
        // 중복시 false 반환
        return userRepository.findByName(name) == null;
    }

    // Oops 회원가입
    public SignInResponse join(SignUpRequest request) {
        User user = User.createOopsUser(request, encoder);
        userRepository.save(user);

        // 토큰에 저장할 user 정보 가져오기
        Long userId = user.getId();

        // Access 토큰 생성
        String accessToken = tokenProvider.createAccessToken(userId);

//        // Refresh 토큰 생성
//        String refreshToken = tokenProvider.createRefreshToken();
//        // Refresh 토큰이 이미 있으면 토큰 갱신, 없으면 토큰 추가
//        userRefreshTokenRepository.findByUserId(userId)
//                .ifPresentOrElse(
//                        it -> it.updateRefreshToken(refreshToken),
//                        () -> userRefreshTokenRepository.save(new UserRefreshToken(user, refreshToken))
//                );

        return new SignInResponse(accessToken);
    }

    // Oops 로그인
    public ResponseEntity signInOops(SignInRequest request) {

        User user = userRepository.findByEmailAndSnsType(request.email(), "oops");

        // 해당하는 이메일이 없을 시
        if (user == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND,
                    ExceptionMessages.NOT_FOUND_EMAIL.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 비밀번호 불일치
        else if (!encoder.matches(request.password(), user.getPassword())) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.BAD_REQUEST,
                    ExceptionMessages.MISS_MATCH_PASSWORD.get()),
                    HttpStatus.BAD_REQUEST);
        }

        // 모두 일치할 시 - 토큰 생성
        String token = tokenProvider.createAccessToken(user.getId());

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new SignInResponse(token)),
                HttpStatus.OK);
    }
}
