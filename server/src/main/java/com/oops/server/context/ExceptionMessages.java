package com.oops.server.context;

public enum ExceptionMessages {

    BAD_REQUEST("요청이 올바르지 않습니다"),
    DUPLICATE_USER("이미 사용 중인 이메일이에요"),
    DUPLICATE_NAME("이미 사용 중인 닉네임이에요"),
    NOT_FOUND_EMAIL("입력된 이메일이 올바르지 않아요"),
    MISS_MATCH_PASSWORD("입력된 비밀번호가 올바르지 않아요"),

    NOT_FOUND_MYPAGE_COMMENT("요약 멘트가 없습니다"),

    NOT_FOUND_RESPONSE_USER("신청하려는 사용자가 존재하지 않습니다"),
    EXIST_FRIEND_REQUEST("이미 친구 신청한 사용자입니다"),
    NOT_FOUND_ACCEPT_USER("수락할 사용자가 존재하지 않습니다"),
    NOT_FOUND_USER("해당 사용자가 존재하지 않습니다"),

    NOT_FOUND_SCHEDULE("해당 날짜에 등록된 일정이 없습니다"),

    DUPLICATE_INVENTORY("이미 있는 이름이에요"),
    NOT_FOUND_INVENTORY("해당 인벤토리가 없습니다"),
    NOT_CREATE_INVENTORY("생성된 인벤토리가 없습니다");

    private final String message;

    ExceptionMessages(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
