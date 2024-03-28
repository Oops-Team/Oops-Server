package com.oops.server.service;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // 이메일을 보내는 메소드
    public void send(String to, String subject, String content) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // 정보 설정
            message.addRecipients(RecipientType.TO, to);
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            // 이메일 발송
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.error("이메일 발송 오류");
            e.printStackTrace();
        }
    }
}
