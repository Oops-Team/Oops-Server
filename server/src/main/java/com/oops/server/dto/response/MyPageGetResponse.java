package com.oops.server.dto.response;

public record MyPageGetResponse(
        String loginType,
        String userEmail,
        String userName,
        String userImgURI,
        Boolean isPublic,
        Boolean isAlert,
        int commentType,
        String comment
) {}
