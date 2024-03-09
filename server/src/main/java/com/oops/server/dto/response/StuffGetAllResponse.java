package com.oops.server.dto.response;

public record StuffGetAllResponse(
        String stuffImgUrl,
        String stuffName,
        boolean isSelected
) {}
