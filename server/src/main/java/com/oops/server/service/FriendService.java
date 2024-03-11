package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.entity.Friend;
import com.oops.server.entity.User;
import com.oops.server.repository.FriendRepository;
import com.oops.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    // 친구 신청
    public ResponseEntity request(Long requestId, String responseName) {
        User requestUser = userRepository.findByUserId(requestId);
        User responseUser = userRepository.findByName(responseName);

        // 친구 신청을 받아야 하는 유저가 존재하지 않을 경우
        if (responseUser == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_RESPONSE_USER.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 이미 친구 신청이 보내진 상태라면
        if (friendRepository.findByRequestUserAndResponseUser(requestUser, responseUser) != null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.CONFLICT,
                            ExceptionMessages.EXIST_FRIEND_REQUEST.get()),
                    HttpStatus.CONFLICT);
        }

        // 레코드 삽입
        friendRepository.save(Friend.create(requestUser, responseUser));

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }
}
