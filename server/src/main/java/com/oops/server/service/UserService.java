package com.oops.server.service;

import com.oops.server.context.ExceptionMessages;
import com.oops.server.context.StatusCode;
import com.oops.server.dto.etc.NoticeDto;
import com.oops.server.dto.request.SignUpRequest;
import com.oops.server.dto.response.DefaultResponse;
import com.oops.server.dto.response.SignInResponse;
import com.oops.server.entity.Notice;
import com.oops.server.entity.User;
//import com.oops.server.entity.UserRefreshToken;
//import com.oops.server.repository.UserRefreshTokenRepository;
import com.oops.server.repository.NoticeRepository;
import com.oops.server.repository.UserRepository;
import com.oops.server.security.TokenProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    //    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

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
    public SignInResponse join(SignUpRequest request) {
        User user = User.create(request, encoder, "oops");
        userRepository.save(user);

        // 토큰에 저장할 user 정보 가져오기
        Long userId = user.getUserId();

        // Access 토큰 생성
        String accessToken = tokenProvider.createAccessToken(userId);

//        // Refresh 토큰 생성
//        String refreshToken = tokenProvider.createRefreshToken();
//        // Refresh 토큰이 이미 있으면 토큰 갱신, 없으면 토큰 추가
//        userRefreshTokenRepository.findByUserId(userId)
//                .ifPresentOrElse(
//                        it -> it.updateRefreshToken(refreshToken),
//                        () -> userRefreshTokenRepository.save(new UserRefreshToken(user, refreshToken))
//                );

        return new SignInResponse(accessToken);
    }

    // Oops 로그인
    public ResponseEntity signInOops(SignUpRequest request) {

        User user = userRepository.findByEmailAndSnsType(request.email(), "oops");

        // 해당하는 이메일이 없을 시
        if (user == null) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.NOT_FOUND,
                    ExceptionMessages.NOT_FOUND_EMAIL.get()),
                    HttpStatus.NOT_FOUND);
        }
        // 비밀번호 불일치
        else if (!encoder.matches(request.password(), user.getPassword())) {
            return new ResponseEntity(DefaultResponse.from(StatusCode.BAD_REQUEST,
                    ExceptionMessages.MISS_MATCH_PASSWORD.get()),
                    HttpStatus.BAD_REQUEST);
        }

        // 모두 일치할 시 - 토큰 생성
        String token = tokenProvider.createAccessToken(user.getUserId());

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new SignInResponse(token)),
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

        // 토큰 발급
        String token = tokenProvider.createAccessToken(user.getUserId());

        return new ResponseEntity(
                DefaultResponse.from(StatusCode.OK, "성공", new SignInResponse(token)),
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

    // 공지사항 모두 조회
    public ResponseEntity getAllNotices() {
        // 공지사항 리스트 가져오기
        List<Notice> noticeList = noticeRepository.getAllNoticeContent();

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
