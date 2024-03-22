package com.oops.server.dto.request;

public record AccountDeleteRequest(
        int reasonType,
        String subReason
) {}
