package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.NoticeDto;
import com.oops.server.dto.request.AccountDeleteRequest;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.MyPageGetResponse;
import com.oops.server.dto.response.SignInResponse;
import com.oops.server.entity.CancelReason;
import com.oops.server.entity.FcmToken;
import com.oops.server.entity.Notice;
import com.oops.server.entity.User;
//import com.oops.server.entity.UserRefreshToken;
//import com.oops.server.repository.UserRefreshTokenRepository;
import com.oops.server.repository.CancelReasonRepository;
import com.oops.server.repository.FcmTokenRepository;
import com.oops.server.repository.NoticeRepository;
import com.oops.server.repository.UserRepository;
import com.oops.server.security.TokenProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final CancelReasonRepository cancelReasonRepository;
    private final NoticeRepository noticeRepository;

    // 멘트 타입
    private final int COMMENT_NOTICE = 1;   // 공지
    private final int COMMENT_TIP = 2;      // tip
    private final int COMMENT_NORMAL = 3;   // 일반

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
    public Map<String, String> join(SignUpRequest request) {
        User user = User.create(request, encoder, "oops");
        user = userRepository.save(user);

        // Access 토큰에 저장할 user 정보 가져오기
        Long userId = user.getUserId();

        // Oops Access 토큰 생성
        String accessToken = tokenProvider.createAccessToken(userId);
        Map<String, String> resTokenMap = new HashMap<>();
        resTokenMap.put("xAuthToken", accessToken);

        // FCM 토큰 저장
        fcmTokenRepository.save(FcmToken.create(user, request.fcmToken()));

        return resTokenMap;
    }

    // Oops 로그인
    public ResponseEntity signInOops(SignUpRequest request) {

        User user = userRepository.findByEmailAndSnsType(request.email(), "oops");

        // 해당하는 이메일이 없을 시
        if (user == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND,
                    ExceptionMessages.MISS_MATCH_EMAIL.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 비밀번호 불일치
        else if (!encoder.matches(request.password(), user.getPassword())) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.BAD_REQUEST,
                    ExceptionMessages.MISS_MATCH_PASSWORD.get()),
                    HttpStatus.BAD_REQUEST);
        }

        // 모두 일치할 시
        // 1. Oops Access 토큰 생성
        String token = tokenProvider.createAccessToken(user.getUserId());
        // 2. FCM 토큰 저장
        FcmToken fcmToken = fcmTokenRepository.findByUserId(user.getUserId());
        // 이미 FCM 토큰이 저장되어 있던 상태라면
        if (fcmToken != null) {
            // 받은 토큰으로 갱신
            fcmToken.modifyToken(request.fcmToken());
            fcmTokenRepository.save(fcmToken);
        }
        // 이미 저장되어 있는 게 없다면
        else {
            // FCM 토큰 데이터 새로 삽입
            fcmTokenRepository.save(FcmToken.create(user, request.fcmToken()));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new SignInResponse(user.getName(), token)),
                HttpStatus.OK);
    }

    // 소셜 로그인
    public ResponseEntity signInSocial(SignUpRequest request, String snsType) {

        String email = request.email();
        User user = userRepository.findByEmailAndSnsType(email, snsType);

        // 가입된 이력이 없을 경우
        if (user == null) {
            // 회원가입 진행
            if (snsType.equals("naver")) {
                user = User.createSocial(request, "naver");
            } else if (snsType.equals("google")) {
                user = User.createSocial(request, "google");
            }

            userRepository.save(user);
            user = userRepository.findByEmailAndSnsType(email, snsType);    // id값을 가져오기 위함
        }

        // 1. Oops Access 토큰 발급
        String token = tokenProvider.createAccessToken(user.getUserId());
        // 2. FCM 토큰 저장
        FcmToken fcmToken = fcmTokenRepository.findByUserId(user.getUserId());
        // 이미 FCM 토큰이 저장되어 있던 상태라면
        if (fcmToken != null) {
            // 받은 토큰으로 갱신
            fcmToken.modifyToken(request.fcmToken());
            fcmTokenRepository.save(fcmToken);
        }
        // 이미 저장되어 있는 게 없다면
        else {
            // FCM 토큰 데이터 새로 삽입
            fcmTokenRepository.save(FcmToken.create(user, request.fcmToken()));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new SignInResponse(user.getName(), token)),
                HttpStatus.OK);
    }

    // 회원 탈퇴
    public ResponseEntity deleteAccount(Long userId, AccountDeleteRequest request) {
        // 해당 회원 정보 삭제
        User user = userRepository.findByUserId(userId);
        userRepository.delete(user);

        // 탈퇴 사유 저장
        cancelReasonRepository.save(CancelReason.create(request.reasonType(), request.subReason()));

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 마이페이지 조회
    public ResponseEntity getMyPage(Long userId) {
        // 1. 사용자 정보 가져오기
        User user = userRepository.findByUserId(userId);

        // 2. 멘트 하나 가져오기
        String comment = "";
        // 2-1. 랜덤으로 돌리기 위한 변수 세팅
        Random random = new Random();
        int commentType = -1;
        // 2-2. 존재하는 글이 뽑힐 때까지..
        boolean[] isEmpty = new boolean[3];
        while (!(isEmpty[0] && isEmpty[1] && isEmpty[2])) {
            // 공지, tip, 일반 멘트 중 하나 랜덤으로 정하기
            commentType = random.nextInt(3) + 1;    // 1 ~ 3

            // 공지 글이 뽑힌 경우
            if (commentType == COMMENT_NOTICE) {
                Notice notice = null;
                try {
                    notice = noticeRepository.getRecentNotice();
                } catch (NullPointerException e) {
                    log.error("가져올 공지 없음");
                }

                // 만일 해당 글이 없다면
                if (notice == null) {
                    isEmpty[COMMENT_NOTICE - 1] = true;
                    commentType = -1;   // 다시 초기화
                    continue;
                }

                // 해당 글 정보 담기
                comment = notice.getTitle();
                break;
            }
            // TIP 글이 뽑힌 경우
            else if (commentType == COMMENT_TIP) {
                List<Notice> noticeList = noticeRepository.findAllByType(COMMENT_TIP);

                // 만일 해당 글이 없다면
                if (noticeList.size() == 0) {
                    isEmpty[COMMENT_TIP - 1] = true;
                    commentType = -1;   // 다시 초기화
                    continue;
                }

                // 뽑아온 글들 중에서 하나 랜덤으로 담아오기
                int randIndex = random.nextInt(noticeList.size());
                comment = noticeList.get(randIndex).getTitle();
                break;
            }
            // 일반 글이 뽑힌 경우
            else {
                List<Notice> noticeList = noticeRepository.findAllByType(COMMENT_NORMAL);

                // 만일 해당 글이 없다면
                if (noticeList.size() == 0) {
                    isEmpty[COMMENT_NORMAL - 1] = true;
                    commentType = -1;   // 다시 초기화
                    continue;
                }

                // 뽑아온 글들 중에서 하나 랜덤으로 담아오기
                int randIndex = random.nextInt(noticeList.size());
                comment = noticeList.get(randIndex).getTitle();
                break;
            }
        }

        // 3. DTO 정보 담기
        MyPageGetResponse myPageGetResponse = new MyPageGetResponse(
                user.getSnsType(),
                user.getEmail(),
                user.getName(),
                user.getProfileUrl(),
                user.isPublic(),
                commentType,
                comment
        );

        // 4. 응답
        // 멘트가 아무것도 뽑히지 않았다면
        if (comment.equals("")) {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK,
                            ExceptionMessages.NOT_FOUND_MYPAGE_COMMENT.get(), myPageGetResponse),
                    HttpStatus.OK);
        }
        // 정상 응답
        else {
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, "성공", myPageGetResponse),
                    HttpStatus.OK);
        }
    }

    // 프로필 공개/비공개 설정 변경
    public ResponseEntity modifyPublic(Long userId, Boolean isPublic) {
        User user = userRepository.findByUserId(userId);
        user.modifyPublic(isPublic);
        userRepository.save(user);

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
    }

    // 공지사항 모두 조회
    public ResponseEntity getAllNotices() {
        // 공지사항 리스트 가져오기
        List<Notice> noticeList = new ArrayList<>();
        try {
            noticeList = noticeRepository.getAllNoticeContent();
        } catch (NullPointerException e) {
            log.error("가져올 공지사항들이 없음");
        }

        // 응답 값에 넣기
        List<NoticeDto> noticeDtoList = new ArrayList<>();
        for (Notice notice : noticeList) {
            noticeDtoList.add(new NoticeDto(
                    notice.getTitle(),
                    notice.getDate(),
                    notice.getContent()
            ));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", noticeDtoList),
                HttpStatus.OK);
    }
}
