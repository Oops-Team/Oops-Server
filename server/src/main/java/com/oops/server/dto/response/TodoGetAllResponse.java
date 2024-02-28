package com.oops.server.dto.response;

import java.time.LocalDate;

public record TodoGetAllResponse(
        LocalDate date,
        Boolean isComplete
) {}
