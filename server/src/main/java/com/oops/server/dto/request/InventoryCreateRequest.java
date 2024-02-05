package com.oops.server.dto.request;

import java.util.List;

public record InventoryCreateRequest(
        String inventoryName,
        List<Integer> inventoryTag
) {}
