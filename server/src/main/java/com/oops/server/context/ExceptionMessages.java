package com.oops.server.context;

public enum ExceptionMessages {

    DUPLICATE_USER("이미 등록된 회원입니다."),
    DUPLICATE_NAME("이미 사용 중인 닉네임입니다."),
    NOT_FOUND_EMAIL("입력된 이메일이 올바르지 않아요."),
    MISS_MATCH_PASSWORD("입력된 비밀번호가 올바르지 않아요."),
    BAD_REQUEST("요청이 올바르지 않습니다."),
    NOT_FOUND_INVENTORY("생성된 인벤토리가 없어요.");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
