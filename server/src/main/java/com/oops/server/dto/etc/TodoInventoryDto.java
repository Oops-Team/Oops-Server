package com.oops.server.dto.etc;

public record TodoInventoryDto(
        Long inventoryId,
        String inventoryName,
        int inventoryIconIdx,
        Boolean isInventoryUsed
) {}
