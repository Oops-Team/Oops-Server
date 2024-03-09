package com.oops.server.dto.request;

import java.time.LocalDate;

public record StuffGetAllRequest(
        LocalDate date,
        Long inventoryId
) { }
