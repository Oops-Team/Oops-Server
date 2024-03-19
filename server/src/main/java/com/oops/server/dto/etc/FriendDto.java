package com.oops.server.dto.etc;

public record FriendDto(
        Long userIdx,
        String userName,
        String userImg
) {}
