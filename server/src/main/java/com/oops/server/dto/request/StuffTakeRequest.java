package com.oops.server.dto.request;

import java.time.LocalDate;

public record StuffTakeRequest(
        LocalDate date,
        String stuffName
) {}