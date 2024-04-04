package com.oops.server.context;

import lombok.NoArgsConstructor;

// 알림 설정 여부 판단 결과를 보내기 위한 Exception Class
@NoArgsConstructor
public class AlertException extends Exception{
    public AlertException(String message) {
        super(message);
    }
}
