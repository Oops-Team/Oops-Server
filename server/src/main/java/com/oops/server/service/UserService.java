package com.oops.server.service;

import static com.oops.server.exception.ExceptionMessages.*;

import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import com.oops.server.token.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;
    private TokenProvider tokenProvider;

    // Oops 회원가입
    public void join(SignUpRequest request) {
        userRepository.save(User.createOopsUser(request, encoder));


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
