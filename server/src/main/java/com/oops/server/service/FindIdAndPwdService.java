package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.User;
import com.oops.server.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindIdAndPwdService {

    private final UserRepository userRepository;

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
}
