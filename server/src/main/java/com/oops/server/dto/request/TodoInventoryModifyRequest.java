package com.oops.server.dto.request;

import java.time.LocalDate;

public record TodoInventoryModifyRequest(
        LocalDate date,
        String inventoryName
) {}
