package com.oops.server.context;

public enum AlertMessages {

    RECEIVE_FRIEND_REQUEST("누군가 회원님과 친구가 되고 싶대요! 확인해 볼까요?"),
    DENY_FRIEND_REQUEST("님이 친구 신청을 거절했어요");

    private final String message;

    AlertMessages(String message) {
        this.message = message;
    }

    public String get() {
        return message;
    }
}
