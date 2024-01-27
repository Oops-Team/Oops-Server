package com.oops.server.service;

import static com.oops.server.context.ExceptionMessages.*;

import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.SignUpResponse;
import com.oops.server.entity.User;
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
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;

    // Oops 회원가입
    public SignUpResponse join(SignUpRequest request) {
        User user = User.createOopsUser(request, encoder);
        userRepository.save(user);

        // 토큰에 저장할 user 정보 가져오기
        Long userId = user.getId();

        // 토큰 생성
        String token = tokenProvider.createAccessToken(userId);

        return new SignUpResponse(token);
    }

    // 중복 회원 검증
    public void validateDuplicateUser(String email, String snsType) {
        if (userRepository.findByEmailAndSnsType(email, snsType) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_USER.get());
        }
    }

    // 중복 닉네임 검증
    public void validateDuplicateName(String name) {
        if (userRepository.findByName(name) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_NAME.get());
        }
    }
}
