package com.oops.server.service;

import static com.oops.server.context.ExceptionMessages.*;

import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.SignUpResponse;
import com.oops.server.entity.User;
import com.oops.server.entity.UserRefreshToken;
import com.oops.server.repository.UserRefreshTokenRepository;
import com.oops.server.repository.UserRepository;
import com.oops.server.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;

    // 중복 회원 검증
    public boolean validateDuplicateUser(String email, String snsType) {
        // 중복시 false 반환
        if (userRepository.findByEmailAndSnsType(email, snsType) != null) {
            return false;
        } else {
            return true;
        }
    }

    // 중복 닉네임 검증
    public boolean validateDuplicateName(String name) {
        // 중복시 false 반환
        if (userRepository.findByName(name) != null) {
            return false;
        }
        else {
            return true;
        }
    }

    // Oops 회원가입
    public SignUpResponse join(SignUpRequest request) {
        User user = User.createOopsUser(request, encoder);
        userRepository.save(user);

        // 토큰에 저장할 user 정보 가져오기
        Long userId = user.getId();

        // Access 토큰 생성
        String accessToken = tokenProvider.createAccessToken(userId);
        // Refresh 토큰 생성
        String refreshToken = tokenProvider.createRefreshToken();
        // Refresh 토큰이 이미 있으면 토큰 갱신, 없으면 토큰 추가
        userRefreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        it -> it.updateRefreshToken(refreshToken),
                        () -> userRefreshTokenRepository.save(new UserRefreshToken(user, refreshToken))
                );

        return new SignUpResponse(accessToken);
    }
}
