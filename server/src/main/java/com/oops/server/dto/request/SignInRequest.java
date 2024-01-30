package com.oops.server.dto.request;

public record SignInRequest(
        String email,
        String password
) {}
