package com.oops.server.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
    public void sendToMessage(Long userId, String body)
            throws NullPointerException, IllegalArgumentException, FirebaseMessagingException {
        // 해당 유저의 FCM 토큰 가져오기
        String token = fcmTokenRepository.findByUserId(userId).getToken();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setBody(body)
                                .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        log.debug("보낸 FCM 메시지 : " + response);
    }
}
