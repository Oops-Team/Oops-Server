package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.NoticeDto;
import com.oops.server.dto.etc.RemindAlertDto;
import com.oops.server.dto.request.AccountDeleteRequest;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.MyPageGetResponse;
import com.oops.server.dto.response.SignInResponse;
import com.oops.server.entity.CancelReason;
import com.oops.server.entity.FcmToken;
import com.oops.server.entity.Notice;
import com.oops.server.entity.Schedule;
import com.oops.server.entity.User;
import com.oops.server.repository.CancelReasonRepository;
import com.oops.server.repository.FcmTokenRepository;
import com.oops.server.repository.NoticeRepository;
import com.oops.server.repository.ScheduleRepository;
import com.oops.server.repository.UserRepository;
import com.oops.server.security.TokenProvider;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final ScheduleRepository scheduleRepository;
    private final CancelReasonRepository cancelReasonRepository;
    private final NoticeRepository noticeRepository;

    // SNS Type 정보
    private final String SNS_OOPS = "oops";
    private final String SNS_NAVER = "naver";
    private final String SNS_GOOGLE = "google";

    // 기본 프로필 이미지 관련 정보
    private final String DEFAULT_PROFILE_NAME = "defaultProfile.png";
    private final String DEFAULT_PROFILE_URL =
            "https://oops-bucket.s3.ap-northeast-2.amazonaws.com/defaultProfile.png";

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

    // Naver 회원가입
    public ResponseEntity joinWithNaver(SignUpRequest request) {
        // 0. 회원가입 진행
        User user = User.createSocial(request, SNS_NAVER);
        user = userRepository.save(user);

        // 1. Oops Access 토큰 발급
        String token = tokenProvider.createAccessToken(user.getUserId());
        Map<String, String> resTokenMap = new HashMap<>();
        resTokenMap.put("xAuthToken", token);

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
                DefaultResponse.from(StatusCode.OK, "성공", resTokenMap),
                HttpStatus.OK);
    }

    // Oops 로그인
    public ResponseEntity signInOops(SignUpRequest request) {

        User user = userRepository.findByEmailAndSnsType(request.email(), SNS_OOPS);

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

        // 3. 일정 알림 리스트 저장
        List<RemindAlertDto> alertList = new ArrayList<>();
        // 오늘 날짜 불러오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Schedule> scheduleList = scheduleRepository.findAllByUserAndDateGreaterThanEqual(user, today);
        for (Schedule schedule : scheduleList) {
            // 해당 날짜
            LocalDate date = schedule.getDate();
            // 외출 시각
            LocalTime outTime = schedule.getOutTime();
            // 리마인더 값
            int[] remindTimeIntArr = Arrays.stream(schedule.getNotification().split(","))
                    .mapToInt(Integer::parseInt).toArray();
            List<Integer> remindList = Arrays.stream(remindTimeIntArr).boxed().toList();

            // 각 정보 dto에 담기
            alertList.add(new RemindAlertDto(date, outTime, remindList));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new SignInResponse(user.getName(), token, alertList)),
                HttpStatus.OK);
    }

    // 소셜 로그인 - Naver(로그인)
    public ResponseEntity signInNaver(SignUpRequest request) {

        String email = request.email();
        User user = userRepository.findByEmailAndSnsType(email, SNS_NAVER);

        // 가입된 이력이 없을 경우
        if (user == null) {
            // 회원가입 요청 응답
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.OK, "회원가입을 진행해주시기 바랍니다"),
                    HttpStatus.OK);
        }

        // 그게 아닐 경우 로그인 진행
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

        // 3. 일정 알림 리스트 저장
        List<RemindAlertDto> alertList = new ArrayList<>();
        // 오늘 날짜 불러오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Schedule> scheduleList = scheduleRepository.findAllByUserAndDateGreaterThanEqual(user, today);
        for (Schedule schedule : scheduleList) {
            // 해당 날짜
            LocalDate date = schedule.getDate();
            // 외출 시각
            LocalTime outTime = schedule.getOutTime();
            // 리마인더 값
            int[] remindTimeIntArr = Arrays.stream(schedule.getNotification().split(","))
                    .mapToInt(Integer::parseInt).toArray();
            List<Integer> remindList = Arrays.stream(remindTimeIntArr).boxed().toList();

            // 각 정보 dto에 담기
            alertList.add(new RemindAlertDto(date, outTime, remindList));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new SignInResponse(user.getName(), token, alertList)),
                HttpStatus.OK);
    }

    // 소셜 로그인 - Google
    public ResponseEntity signInGoogle(SignUpRequest request) {

        String email = request.email();
        User user = userRepository.findByEmailAndSnsType(email, SNS_GOOGLE);

        // 가입된 이력이 없을 경우
        if (user == null) {
            // 회원가입 진행
            user = User.createSocial(request, SNS_GOOGLE);

            user = userRepository.save(user);    // id값을 가져오기 위함
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

        // 3. 일정 알림 리스트 저장
        List<RemindAlertDto> alertList = new ArrayList<>();
        // 오늘 날짜 불러오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        List<Schedule> scheduleList = scheduleRepository.findAllByUserAndDateGreaterThanEqual(user, today);
        for (Schedule schedule : scheduleList) {
            // 해당 날짜
            LocalDate date = schedule.getDate();
            // 외출 시각
            LocalTime outTime = schedule.getOutTime();
            // 리마인더 값
            int[] remindTimeIntArr = Arrays.stream(schedule.getNotification().split(","))
                    .mapToInt(Integer::parseInt).toArray();
            List<Integer> remindList = Arrays.stream(remindTimeIntArr).boxed().toList();

            // 각 정보 dto에 담기
            alertList.add(new RemindAlertDto(date, outTime, remindList));
        }

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공",
                        new SignInResponse(user.getName(), token, alertList)),
                HttpStatus.OK);
    }

    // 회원 탈퇴
    public ResponseEntity deleteAccount(Long userId, AccountDeleteRequest request) {
        // 해당 회원 정보 불러오기
        User user = userRepository.findByUserId(userId);

        // 해당 회원이 기본 프로필을 사용하고 있지 않을 경우
        // 해당 회원의 프로필 사진 데이터(S3) 삭제
        String profileImgUrl = user.getProfileUrl();
        String profileImgName = profileImgUrl.split("/")[3];
        if (!profileImgName.equals(DEFAULT_PROFILE_NAME)) {
            s3Service.deleteFile(profileImgName);
        }

        // 해당 회원 정보 삭제
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
        // 만일 기본 프로필일 경우 null로 설정
        String profileUrl = user.getProfileUrl();
        if (profileUrl.equals(DEFAULT_PROFILE_URL)) {
            profileUrl = null;
        }
        MyPageGetResponse myPageGetResponse = new MyPageGetResponse(
                user.getSnsType(),
                user.getEmail(),
                user.getName(),
                profileUrl,
                user.getIsPublic(),
                user.getIsAlert(),
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

    // 프로필 사진 변경
    public ResponseEntity modifyProfileImage(Long userId, MultipartFile imageFile) {
        User user = userRepository.findByUserId(userId);

        // 기존 프로필 사진 파일 이름 가져오기
        String oldImageUrl = user.getProfileUrl();
        String oldImageName = oldImageUrl.split("/")[3];
        log.debug("oldImageName: " + oldImageName);

        // 새로 설정할 프로필 사진의 파일 이름 가져오기
        String newImageName = imageFile.getOriginalFilename();
        log.debug("newImageName: " + newImageName);

        // 기존 프로필 사진과 동일한 사진으로 변경하는 경우
        if (newImageName.equals(oldImageName)) {
            // 프론트단에 실패 응답 (409)
            return new ResponseEntity(
                    DefaultResponse.from(StatusCode.CONFLICT,
                            ExceptionMessages.CONFLICT_PROFILE_IMAGE.get()),
                    HttpStatus.CONFLICT);
        }

        // 기본 프로필로 설정하는 경우
        if (newImageName.equals(DEFAULT_PROFILE_NAME)) {
            user.modifyProfileUrl(DEFAULT_PROFILE_URL);
            userRepository.save(user);
        }
        // 새로운 프로필로 설정할 경우
        else {
            // 새 프로필 사진 업로드
            String newImageUrl = "";
            try {
                newImageUrl = s3Service.uploadFile(imageFile);
            } catch (IOException e) {
                log.error("새 프로필 사진 업로드 실패");
                e.printStackTrace();

                // 프론트단에 실패 응답 (500)
                return new ResponseEntity(
                        DefaultResponse.from(StatusCode.INTERNAL_SERVER_ERROR,
                                ExceptionMessages.FAILED_PROFILE_IMAGE_UPLOAD.get()),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 새 프로필 사진의 url 저장
            user.modifyProfileUrl(newImageUrl);
            userRepository.save(user);
        }

        // 기존 프로필이 기본 프로필이 아니라면
        if (!oldImageName.equals(DEFAULT_PROFILE_NAME)) {
            // 기존 프로필 사진 삭제
            s3Service.deleteFile(oldImageName);
        }

        // 성공 응답
        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공"),
                HttpStatus.OK);
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

    // 푸시알림 설정 변경
    public ResponseEntity modifyAlertSetting(Long userId, Boolean isAlert) {
        User user = userRepository.findByUserId(userId);
        user.modifyAlertSetting(isAlert);
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
