package com.oops.server.context;

public enum ExceptionMessages {

    REFRESH_PAGE("페이지 새로고침이 필요합니다"),

    BAD_REQUEST("요청이 올바르지 않습니다"),
    DUPLICATE_USER("이미 사용 중인 이메일이에요"),
    DUPLICATE_NAME("이미 사용 중인 닉네임이에요"),
    MISS_MATCH_EMAIL("입력된 이메일이 올바르지 않아요"),
    MISS_MATCH_PASSWORD("입력된 비밀번호가 올바르지 않아요"),

    NOT_FOUND_EMAIL("등록된 이메일이 없습니다"),
    EXPIRATION_VERIFICATION_CODE("인증 정보가 만료되었습니다"),
    MISS_MATCH_VERIFICATION_CODE("인증코드가 일치하지 않습니다"),
    CONFLICT_OLD_PASSWORD("기존 비밀번호와 일치해 사용할 수 없어요"),

    CONFLICT_PROFILE_IMAGE("기존 프로필 사진과 동일한 사진입니다"),
    FAILED_PROFILE_IMAGE_UPLOAD("프로필 사진 업로드를 실패했습니다"),
    NOT_FOUND_MYPAGE_COMMENT("요약 멘트가 없습니다"),

    NOT_ALLOWED_ALERT("요청 수행은 성공했으나, 알림 설정 해제로 인해 알림을 보낼 수 없습니다"),
    NOT_FOUND_FCM_TOKEN("해당 사용자의 FCM 토큰 데이터가 없습니다"),
    WRONG_FCM_TOKEN("올바르지 않은 FCM 토큰입니다"),

    NOT_FOUND_FRIENDS("불러올 친구가 없습니다"),
    DO_NOT_STING("콕콕 찌르기를 할 수 없습니다"),
    FAILED_STING("콕콕 찌르기를 실패했습니다"),

    NOT_FOUND_RESPONSE_USER("신청하려는 사용자가 존재하지 않습니다"),
    EXIST_FRIEND_REQUEST("이미 친구 신청한 사용자입니다"),
    NOT_FOUND_ACCEPT_USER("수락할 사용자가 존재하지 않습니다"),
    NOT_FOUND_USER("해당 사용자가 존재하지 않습니다"),
    FAILED_FRIEND_REQUEST_ALERT("친구 신청 알림을 보내지 못했습니다"),
    FAILED_FRIEND_ACCEPT_ALERT("친구 수락 알림을 보내지 못했습니다"),
    FAILED_FRIEND_DENY_ALERT("친구 거절 알림을 보내지 못했습니다"),

    NOT_FOUND_SCHEDULE("해당 날짜에 등록된 일정이 없습니다"),

    EXCEED_INVENTORY("인벤토리 최대 생성 개수를 초과했습니다"),
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
