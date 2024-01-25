package com.oops.server.dto.response;

public record SignUpResponse (
        String message,
        int status,
        String token,
        Long userId
) {}
