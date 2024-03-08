package com.oops.server.dto.request;

import java.time.LocalDate;
import java.util.List;

public record TodoStuffAddRequest(
        LocalDate date,
        List<String> stuffName,
        boolean isAddInventory,
        Long inventoryId
) {}
