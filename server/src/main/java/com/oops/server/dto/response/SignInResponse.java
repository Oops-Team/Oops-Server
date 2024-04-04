package com.oops.server.dto.response;

public record SignInResponse(
        String name,
        String xAuthToken
) {}
