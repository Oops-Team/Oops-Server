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

        // 이미 친구 신청이 보내진 상태라면 (양방향 모두 검색)
        if (friendRepository.findByRequestUserAndResponseUser(requestUser, responseUser) != null
                || friendRepository.findByRequestUserAndResponseUser(responseUser, requestUser)
                != null) {
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

    // 친구 수락
    public ResponseEntity accept(Long myId, Long friendId) {
        // 친구 신청 내역 가져오기
        User me = userRepository.findByUserId(myId);
        User friend = userRepository.findByUserId(friendId);
        Friend friendRelation = friendRepository.findByRequestUserAndResponseUser(friend, me);

        try {
            // 친구 신청 수락 진행
            friendRelation.acceptRequest();
            friendRepository.save(friendRelation);
        } catch (NullPointerException e) {
            // 수락할 친구가 존재하지 않을 경우 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_ACCEPT_USER.get()),
                    HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 친구 신청 거절 & 친구 삭제
    public ResponseEntity delete(Long myId, Long friendId) {
        // 친구 신청 내역 가져오기
        User me = userRepository.findByUserId(myId);
        User friend = userRepository.findByUserId(friendId);
        Friend friendRelation = friendRepository.findByRequestUserAndResponseUser(friend, me);
        // 양방향 검색을 위함
        friendRelation = (friendRelation != null) ? friendRelation
                : friendRepository.findByRequestUserAndResponseUser(me, friend);

        // (양방향 검색을 했는데도) 삭제 & 거절할 친구가 없을 경우
        if (friendRelation == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_USER.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 삭제 & 거절 수행
        friendRepository.delete(friendRelation);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", friendId),
                HttpStatus.OK);
    }
}
