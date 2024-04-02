package com.oops.server.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.oops.server.context.AlertMessages;
import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.FriendDto;
import com.oops.server.dto.request.StingFriendRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.FriendGetAllResponse;
import com.oops.server.dto.response.FriendGetSearchResponse;
import com.oops.server.entity.Friend;
import com.oops.server.entity.User;
import com.oops.server.repository.FcmTokenRepository;
import com.oops.server.repository.FriendRepository;
import com.oops.server.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FcmTokenRepository fcmTokenRepository;

    // 친구 관계 상태값
    private final int IS_FRIEND = 1;            // 현재 친구O
    private final int PENDING_OUTGOING = 2;     // 보낸 친구 요청 (대기중)
    private final int PENDING_INCOMING = 3;     // 받은 친구 요청 (대기중)

    // 콕콕 찌르기 친구들 불러오는 기준 시간 값
    // ex) 30일 경우, 외출 30분 전인 친구를 조회하는 것
    private final int STING_AFTER_TIME = 30;
    // 콕콕 찌르기 화면에 뜨는 최대 친구 수
    private final int STING_LIST_MAXIMUM = 5;

    // 외출 30분 전인 친구 조회 (찌르기 화면)
    public ResponseEntity getStingList(Long userId) {
        User user = userRepository.findByUserId(userId);

        // 현재 날짜 구하기
        LocalDate presentDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.debug("현재 날짜 : " + presentDate);

        // 현재 시각 구하기
        LocalTime presentTime = LocalTime.now(ZoneId.of("Asia/Seoul"));
        log.debug("현재 시각 : " + presentTime.toString());

        // 시간 간격 구하기
        LocalTime endTime = presentTime.plusMinutes(STING_AFTER_TIME);
        log.debug("end 시각 : " + endTime.toString());

        // 찌를 수 있는 친구 모두 불러오기
        List<User> stingFriendList = new ArrayList<>();
        try {
            stingFriendList = friendRepository.getStingList(user, presentDate, presentTime, endTime);
        } catch (NullPointerException e) {
            log.error("친구 조회 실패");
            e.printStackTrace();

            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND, ExceptionMessages.NOT_FOUND_FRIENDS.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 응답 DTO
        List<FriendDto> friendDtoList = new ArrayList<>();

        // 찌를 수 있는 친구가 5명을 초과한다면
        if (stingFriendList.size() > STING_LIST_MAXIMUM) {
            Random random = new Random();
            int listSize = stingFriendList.size();

            // 랜덤으로 5명 담기
            for (int i = 0; i < STING_LIST_MAXIMUM; i++) {
                // 랜덤 index 뽑기
                int randIndex = random.nextInt(listSize);

                // dto 정보 넣기
                friendDtoList.add(new FriendDto(
                        stingFriendList.get(randIndex).getUserId(),
                        stingFriendList.get(randIndex).getName(),
                        stingFriendList.get(randIndex).getProfileUrl()
                ));

                // 해당 친구 리스트에서 제거
                stingFriendList.remove(randIndex);
                // size 줄이기
                listSize--;
            }

            // 해당 친구 정보 return
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, "성공", friendDtoList),
                    HttpStatus.OK);
        }

        // (친구가 5명 이하일 경우) 친구 정보 다 담기
        for (User friend : stingFriendList) {
            friendDtoList.add(new FriendDto(
                    friend.getUserId(),
                    friend.getName(),
                    friend.getProfileUrl()
            ));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", friendDtoList),
                HttpStatus.OK);
    }

    // 친구 찌르기
    public ResponseEntity stingFriend(StingFriendRequest request) {
        // 1. 알림을 받는 유저의 정보(FCM 토큰) 가져오기
        User resUser = userRepository.findByName(request.name());
        // 만일 해당 유저가 없다면
        if (resUser == null) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_USER.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 만일 해당 유저에게 토큰이 없다면
        String resUserFcmToken = "";
        try {
            resUserFcmToken = fcmTokenRepository.findByUserId(resUser.getUserId()).getToken();
        } catch (NullPointerException e) {
            log.error("FCM 토큰 없음");

            // 프론트단에 실패 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_FCM_TOKEN.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 2. 알림 보내기
        try {
            FcmService.sendToMessage(resUserFcmToken, request.body());
        } catch (FirebaseMessagingException e) {
            log.error("콕콕 찌르기 실패");
            e.printStackTrace();

            // 프론트단에 실패 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.INTERNAL_SERVER_ERROR,
                            ExceptionMessages.FAILED_STING.get()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException e) {
            log.error("해당 사용자의 토큰 없음");

            // 프론트단에 실패 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.FAILED_STING.get()),
                    HttpStatus.NOT_FOUND);
        }

        // 3. 프론트단에 성공 응답
        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 친구 리스트 전체 조회
    public ResponseEntity getAll(Long userId) {
        User user = userRepository.findByUserId(userId);

        // 응답 DTO
        List<FriendGetAllResponse> data = new ArrayList<>();

        // 1-1. 내가 요청받았던 입장의 친구 목록 불러오기
        List<User> receiveFriendList = new ArrayList<>();
        try {
            receiveFriendList = friendRepository.getIncomingFriendRequestList(user);
        } catch (NullPointerException e) {
            log.error("요청 받은 목록 없음!");
        }

        // 1-2. 1번 항목 응답 정보에 담기
        for (User friendUser : receiveFriendList) {
            data.add(new FriendGetAllResponse(
                    friendUser.getUserId(),
                    friendUser.getName(),
                    friendUser.getProfileUrl(),
                    PENDING_INCOMING));
        }

        // 2-1. 내가 요청했던 입장의 친구 목록 불러오기
        List<User> requestFriendList = new ArrayList<>();
        try {
            requestFriendList = friendRepository.getSendFriendRequestList(user);
        } catch (NullPointerException e) {
            log.error("내가 요청 보낸 목록 없음!");
        }

        // 2-2. 2번 항목 응답 정보에 담기
        for (User friendUser : requestFriendList) {
            data.add(new FriendGetAllResponse(
                    friendUser.getUserId(),
                    friendUser.getName(),
                    friendUser.getProfileUrl(),
                    PENDING_OUTGOING));
        }

        // 3-1. 나와 완전 친구인 사용자들 불러오기
        List<User> perfectFriendList = new ArrayList<>();
        try {
            perfectFriendList = friendRepository.getFriendList(user);
        } catch (NullPointerException e) {
            log.error("나와 완전 친구인 사용자 없음!");
        }

        // 3-2. 3번 항목 응답 정보에 담기
        for (User friendUser : perfectFriendList) {
            data.add(new FriendGetAllResponse(
                    friendUser.getUserId(),
                    friendUser.getName(),
                    friendUser.getProfileUrl(),
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
        List<User> friendList = new ArrayList<>();
        try {
            friendList = friendRepository.getSearchFriendList(user, searchName);
        } catch (NullPointerException e) {
        }

        // 친구 중에서 검색한 결과 dto 정보 넣기
        List<FriendDto> friendDtoList = new ArrayList<>();
        for (User friendUser : friendList) {
            friendDtoList.add(new FriendDto(
                    friendUser.getUserId(),
                    friendUser.getName(),
                    friendUser.getProfileUrl()));
        }

        // 해당 사용자와 친구가 아닌 사람들 중 검색
        List<User> notFriendList = new ArrayList<>();
        try {
            notFriendList = userRepository.getSearchNotFriendList(user.getUserId(), searchName);
        } catch (NullPointerException e) {
        }

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

        // 친구 신청을 받은 사용자에게 알림 전송
        String resUserFcmToken = "";
        try {
            resUserFcmToken = fcmTokenRepository.findByUserId(responseUser.getUserId()).getToken();
        } catch (NullPointerException e) {
            log.error("알림을 받을 사용자의 FCM 토큰 없음");

            // 프론트단에 실패 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.NOT_FOUND,
                            ExceptionMessages.NOT_FOUND_FCM_TOKEN.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 알림 보내기
        try {
            FcmService.sendToMessage(resUserFcmToken, AlertMessages.RECEIVE_FRIEND_REQUEST.get());
        } catch (FirebaseMessagingException e) {
            log.error("친구 신청을 했을 경우 알림 전송 실패");
            e.printStackTrace();

            // 프론트단에 실패 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.INTERNAL_SERVER_ERROR,
                            ExceptionMessages.FAILED_FRIEND_REQUEST_ALERT.get()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

            // 거절 당한 사용자에게 알림 보내기
            String resUserFcmToken = "";
            try {
                resUserFcmToken = fcmTokenRepository.findByUserId(friend.getUserId()).getToken();
            } catch (NullPointerException e) {
                log.error("알림을 받을 사용자의 FCM 토큰 없음");

                // 프론트단에 실패 응답
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.NOT_FOUND,
                                ExceptionMessages.NOT_FOUND_FCM_TOKEN.get()),
                        HttpStatus.NOT_FOUND);
            }
            // 알림 보내기
            try {
                FcmService.sendToMessage(resUserFcmToken, AlertMessages.DENY_FRIEND_REQUEST.get());
            } catch (FirebaseMessagingException e) {
                log.error("친구 신청을 거절했을 경우 알림 전송 실패");
                e.printStackTrace();

                // 프론트단에 실패 응답
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.INTERNAL_SERVER_ERROR,
                                ExceptionMessages.FAILED_FRIEND_REQUEST_ALERT.get()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", friendId),
                HttpStatus.OK);
    }
}
