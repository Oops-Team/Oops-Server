package com.oops.server.dto.response;

public record SignUpResponse (
        String accessToken,
        String refreshToken
) {}
