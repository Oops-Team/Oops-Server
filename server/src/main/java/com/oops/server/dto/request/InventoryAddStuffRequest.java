package com.oops.server.dto.request;

import java.util.List;

public record InventoryAddStuffRequest(
        List<String> stuffName
) {}
