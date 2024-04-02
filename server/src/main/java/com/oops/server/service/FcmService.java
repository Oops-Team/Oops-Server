package com.oops.server.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FcmService {

    // FCM 서버로 알림 요청을 보내는 메소드
    public static void sendToMessage(String token, String body)
            throws FirebaseMessagingException, IllegalArgumentException {
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
