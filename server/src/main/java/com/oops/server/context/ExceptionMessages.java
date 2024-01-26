package com.oops.server.context;

public enum ExceptionMessages {

    DUPLICATE_USER("이미 등록된 회원입니다."),
    DUPLICATE_NAME("이미 사용 중인 닉네임입니다.");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
