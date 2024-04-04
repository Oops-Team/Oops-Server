package com.oops.server.dto.request;

public record SignUpRequest (
        String name,
        String email,
        String password,
        String fcmToken,
        Boolean isAlert
) {}