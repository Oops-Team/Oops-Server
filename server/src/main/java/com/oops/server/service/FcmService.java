package com.oops.server.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.oops.server.context.AlertException;
import com.oops.server.context.ExceptionMessages;
import com.oops.server.entity.User;
import com.oops.server.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    // FCM 서버로 알림 요청을 보내는 메소드
    public void sendToMessage(User user, String body)
            throws AlertException, NullPointerException, IllegalArgumentException, FirebaseMessagingException {
        // 해당 유저의 알림 설정 여부 확인
        boolean isAlert = user.getIsAlert();
        // 알림이 해제된 상태일 경우
        if (!isAlert) {
            throw new AlertException(ExceptionMessages.NOT_ALLOWED_ALERT.get());
        }

        // 해당 유저의 FCM 토큰 가져오기
        String token = fcmTokenRepository.findByUserId(user.getUserId()).getToken();

        // Alert 내용 구성
        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setBody(body)
                                .build())
                .build();

        // Alert 전송
        FirebaseMessaging.getInstance().send(message);
        log.info("FCM 전송 성공!");
    }
}
