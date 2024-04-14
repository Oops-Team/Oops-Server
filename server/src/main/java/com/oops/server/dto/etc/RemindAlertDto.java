package com.oops.server.dto.etc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RemindAlertDto(
        LocalDate date,
        LocalTime outTime,
        List<Integer> remindList
) {}
