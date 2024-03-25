package com.oops.server.dto.request;

public record PwdCodeVerificationRequest(
        String code,
        String email
) {}
