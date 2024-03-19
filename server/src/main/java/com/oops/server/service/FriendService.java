package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.FriendDto;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.FriendGetAllResponse;
import com.oops.server.dto.response.FriendGetSearchResponse;
import com.oops.server.entity.Friend;
import com.oops.server.entity.User;
import com.oops.server.repository.FriendRepository;
import com.oops.server.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    // 친구 관계 상태값
    private final int IS_FRIEND = 1;            // 현재 친구O
    private final int PENDING_OUTGOING = 2;     // 보낸 친구 요청 (대기중)
    private final int PENDING_INCOMING = 3;     // 받은 친구 요청 (대기중)

    // 친구 리스트 전체 조회
    public ResponseEntity getAll(Long userId) {
        User user = userRepository.findByUserId(userId);

        // 응답 DTO
        List<FriendGetAllResponse> data = new ArrayList<>();

        // 1-1. 내가 요청받았던 입장의 친구 목록 불러오기
        List<Friend> receiveFriendList = friendRepository.getIncomingFriendRequestList(user);

        // 1-2. 1번 항목 응답 정보에 담기
        for (Friend friend : receiveFriendList) {
            data.add(new FriendGetAllResponse(
                    friend.getRequestUser().getUserId(),
                    friend.getRequestUser().getName(),
                    friend.getRequestUser().getProfileUrl(),
                    PENDING_INCOMING));
        }

        // 2-1. 내가 요청했던 입장의 친구 목록 불러오기
        List<Friend> requestFriendList = friendRepository.getSendFriendRequestList(user);

        // 2-2. 2번 항목 응답 정보에 담기
        for (Friend friend : requestFriendList) {
            data.add(new FriendGetAllResponse(
                    friend.getResponseUser().getUserId(),
                    friend.getResponseUser().getName(),
                    friend.getResponseUser().getProfileUrl(),
                    PENDING_OUTGOING));
        }

        // 3-1. 나와 완전 친구인 사용자들 불러오기
        List<Friend> perfectFriendList = friendRepository.getFriendList(user);

        // 3-2. 3번 항목 응답 정보에 담기
        for (Friend friend : perfectFriendList) {
            data.add(new FriendGetAllResponse(
                    friend.getResponseUser().getUserId(),
                    friend.getResponseUser().getName(),
                    friend.getResponseUser().getProfileUrl(),
                    IS_FRIEND));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", data),
                HttpStatus.OK);
    }

    // 사용자 리스트 검색 조회
    public ResponseEntity getSearch(Long userId, String name) {
        User user = userRepository.findByUserId(userId);
        String searchName = "%" + name + "%"; // 해당 문자열을 포함하는 닉네임을 검색하기 위함

        // 해당 사용자와 완전 친구인 사람들 중 검색
        List<User> friendList = friendRepository.getSearchFriendList(user, searchName);
        // 친구 중에서 검색한 결과 dto 정보 넣기
        List<FriendDto> friendDtoList = new ArrayList<>();
        for (User friendUser : friendList) {
            friendDtoList.add(new FriendDto(
                    friendUser.getUserId(),
                    friendUser.getName(),
                    friendUser.getProfileUrl()));
        }

        // 해당 사용자와 친구가 아닌 사람들 중 검색
        List<User> notFriendList = userRepository.getSearchNotFriendList(user.getUserId(), searchName);
        // 친구가 아닌 사용자들 중에서 검색한 결과 dto 정보 넣기
        List<FriendDto> notFriendDtoList = new ArrayList<>();
        for (User notFriendUser : notFriendList) {
            notFriendDtoList.add(new FriendDto(
                    notFriendUser.getUserId(),
                    notFriendUser.getName(),
                    notFriendUser.getProfileUrl()));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new FriendGetSearchResponse(friendDtoList, notFriendDtoList)),
                HttpStatus.OK);
    }

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

            // 역방향 행 삽입
            friendRepository.save(Friend.createFriendTrue(me, friend));
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

        // 삭제 & 거절할 친구가 없을 경우
        if (friendRelation == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_USER.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 삭제 & 거절 수행
        // 만일 친구 삭제의 상황이라면
        if (friendRelation.isFriend()) {
            // 요청인, 피요청인 저장
            User requestUser = friendRelation.getRequestUser();
            User responseUser = friendRelation.getResponseUser();

            // 정방향 행 삭제
            friendRepository.delete(friendRelation);

            // 역방향 행 삭제
            Friend reverseFriend = friendRepository.findByRequestUserAndResponseUser(responseUser,
                    requestUser);
            friendRepository.delete(reverseFriend);
        }
        // 만일 친구 신청 거절의 상황이라면
        else {
            // 해당 행 삭제
            friendRepository.delete(friendRelation);
        }


        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", friendId),
                HttpStatus.OK);
    }
}
