package com.oops.server.service;

import static com.oops.server.exception.ExceptionMessages.*;

import com.oops.server.dto.user.SignUpRequest;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Oops 회원가입
    public void join(SignUpRequest request) {
        User user = new User();
        user.createOopsBuilder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        userRepository.save(user);
    }

    // 중복 회원 검증
    public void validateDuplicateUser(String email, String snsType) {
        if (userRepository.findByEmailAndSnsType(email, snsType) != null) {
            throw new RuntimeException(DUPLICATE_USER.get());
        }
    }

    // 중복 닉네임 검증
    public void validateDuplicateName(String name) {
        if (userRepository.findByName(name) != null) {
            throw new RuntimeException(DUPLICATE_NAME.get());
        }
    }
}
