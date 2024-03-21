package com.oops.server.dto.etc;

import java.time.LocalDate;

public record NoticeDto(
        String noticeTitle,
        LocalDate date,
        String content
) {}
