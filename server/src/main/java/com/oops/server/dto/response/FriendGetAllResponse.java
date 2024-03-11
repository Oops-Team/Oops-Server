package com.oops.server.dto.response;

public record FriendGetAllResponse(
        Long userIdx,
        String userName,
        String userImg,
        int userState
) {}
