package com.oops.server.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TodoCreateRequest(
     LocalDate date,
     List<String> todoName,
     List<Integer> todoTag,
     LocalTime goOutTime,
     List<Integer> remindTime
) {}
